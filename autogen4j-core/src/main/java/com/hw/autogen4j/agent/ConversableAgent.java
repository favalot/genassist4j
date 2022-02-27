
/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hw.autogen4j.agent;

import com.google.common.collect.Lists;
import com.hw.autogen4j.entity.*;
import com.hw.autogen4j.exception.Autogen4jException;
import com.hw.openai.OpenAiClient;
import com.hw.openai.entity.chat.*;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.hw.autogen4j.entity.HumanInputMode.*;
import static com.hw.autogen4j.util.CodeUtil.executeCode;
import static com.hw.autogen4j.util.CodeUtil.extractCode;
import static com.hw.openai.entity.chat.ChatMessageRole.*;

/**
 * A class for generic conversable agents which can be configured as assistant or user proxy.
 * <p>
 * After receiving each message, the agent will send a reply to the sender unless the msg is a termination msg.
 * For example, AssistantAgent and UserProxyAgent are subclasses of this class, configured with different settings.
 *
 * @author HamaWhite
 */
public class ConversableAgent extends Agent {

    private static final Logger LOG = LoggerFactory.getLogger(ConversableAgent.class);

    private static final String NO_HUMAN_INPUT_MSG = "NO HUMAN INPUT RECEIVED.";

    /**
     * a function that takes a message in the form of a dictionary and
     * returns a boolean value indicating if this received message is a termination message.
     */
    protected Predicate<ChatMessage> isTerminationMsg;

    /**
     * maximum number of consecutive auto replies
     */
    protected int maxConsecutiveAutoReply;

    /**
     * whether to ask for human inputs every time a message is received.
     */
    protected HumanInputMode humanInputMode;

    /**
     * mapping function names (passed to llm) to functions.
     */
    protected Map<String, Function<?, ?>> functionMap;

    /**
     * config for the code execution.
     */
    protected CodeExecutionConfig codeExecutionConfig;

    /**
     * a client for interacting with the OpenAI API.
     */
    protected OpenAiClient client;

    /**
     * Chat conversation.
     */
    protected ChatCompletion chatCompletion;

    /**
     * default auto reply when no code execution or llm-based reply is generated.
     */
    protected String defaultAutoReply;

    private final Map<Agent, Integer> consecutiveAutoReplyCounter = new HashMap<>();
    private final List<ChatMessage> oaiSystemMessage;
    private final Map<Agent, List<ChatMessage>> oaiMessages = new HashMap<>();

    private final List<BiFunction<Agent, List<ChatMessage>, ReplyResult>> replyFuncList;

    protected ConversableAgent(Builder<?> builder) {
        this.name = builder.name;
        this.systemMessage = builder.systemMessage;
        this.isTerminationMsg = builder.isTerminationMsg;
        this.maxConsecutiveAutoReply = builder.maxConsecutiveAutoReply;
        this.humanInputMode = builder.humanInputMode;
        this.functionMap = builder.functionMap;
        this.codeExecutionConfig = builder.codeExecutionConfig;
        this.client = builder.client;
        this.chatCompletion = builder.chatCompletion;
        this.defaultAutoReply = builder.defaultAutoReply;

        this.oaiSystemMessage = List.of(new ChatMessage(SYSTEM, systemMessage));
        // creating a list of method references
        this.replyFuncList = Lists.newArrayList(
                this::checkTerminationAndHumanReply,
                this::generateFunctionCallReply,
                this::generateCodeExecutionReply,
                this::generateOaiReply);
    }

    /**
     * The reply function will be called when the trigger matches the sender.
     * The function registered later will be checked earlier by default.
     *
     * @param replyFunc the reply function.
     */
    protected void registerReply(BiFunction<Agent, List<ChatMessage>, ReplyResult> replyFunc) {
        this.replyFuncList.add(0, replyFunc);
    }

    /**
     * Update the system message.
     *
     * @param systemMessage system message for the ChatCompletion inference.
     */
    public void updateSystemMessage(String systemMessage) {
        oaiSystemMessage.get(0).setContent(systemMessage);
    }

    /**
     * The last message exchanged with the agent.
     *
     * @param agent The agent in the conversation.
     *              If None and more than one agent's conversations are found, an error will be raised.
     *              If None and only one conversation is found, the last message of the only conversation will be returned.
     * @return The last message exchanged with the agent.
     */
    protected ChatMessage lastMessage(Agent agent) {
        if (!oaiMessages.containsKey(agent)) {
            throw new Autogen4jException(
                    "The agent %s is not present in any conversation. No history available for this agent.",
                    agent.getName());
        }
        return oaiMessages.get(agent).get(oaiMessages.get(agent).size() - 1);
    }

    /**
     * Append a message to the ChatCompletion conversation.
     */
    private void appendOaiMessage(Agent agent, ChatMessage message, ChatMessageRole role) {
        ChatMessage oaiMessage = new ChatMessage(message);
        if (!FUNCTION.equals(message.getRole())) {
            oaiMessage.setRole(role);
        }
        oaiMessages.computeIfAbsent(agent, key -> new ArrayList<>()).add(oaiMessage);
    }

    @Override
    public void send(Agent recipient, ChatMessage message, boolean requestReply, boolean silent) {
        // when the agent composes and sends the message, the role of the message is "assistant" unless it's "function".
        appendOaiMessage(recipient, message, ASSISTANT);

        recipient.receive(this, message, requestReply, silent);
    }

    private void printReceivedMessage(Agent sender, ChatMessage message) {
        LOG.info("{} (to {}):\n", sender.getName(), this.getName());

        if (FUNCTION.equals(message.getRole())) {
            String funcPrint = String.format("***** Response from calling function '%s' *****", message.getName());
            LOG.info(funcPrint);
            LOG.info(message.getContent());

            String repeatedStars = "*".repeat(funcPrint.length());
            LOG.info(repeatedStars);
        } else {
            if (StringUtils.isNotEmpty(message.getContent())) {
                LOG.info(message.getContent());
            }
            if (CollectionUtils.isNotEmpty(message.getToolCalls())) {
                // only support the first function now.
                FunctionCall functionCall = message.getToolCalls().get(0).getFunction();
                String funcPrint = String.format("***** Suggested function Call: %s *****", functionCall.getName());
                LOG.info(funcPrint);
                LOG.info("Arguments: \n{}", functionCall.getArguments());

                String repeatedStars = "*".repeat(funcPrint.length());
                LOG.info(repeatedStars);
            }
        }
        String repeatedHyphens = "\n" + "-".repeat(80);
        LOG.info(repeatedHyphens);
    }

    private void processReceivedMessage(Agent sender, ChatMessage message, boolean silent) {
        // when the agent receives a message, the role of the message is "user" unless it's "function".
        appendOaiMessage(sender, message, USER);

        if (!silent) {
            printReceivedMessage(sender, message);
        }
    }