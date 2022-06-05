
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

package com.hw.autogen4j.agent.group;

import com.hw.autogen4j.agent.Agent;
import com.hw.autogen4j.agent.ConversableAgent;
import com.hw.autogen4j.entity.ReplyResult;
import com.hw.autogen4j.exception.Autogen4jException;
import com.hw.openai.entity.chat.ChatMessage;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hw.openai.entity.chat.ChatMessageRole.SYSTEM;

/**
 * @author HamaWhite
 */
@Getter
@Builder
public class GroupChat {

    private static final Logger LOG = LoggerFactory.getLogger(GroupChat.class);

    /**
     * the name of the admin agent if there is one.
     */
    @Builder.Default
    private String adminName = "Admin";

    /**
     * a list of participating agents.
     */
    private List<Agent> agents;

    /**
     * a list of messages in the group chat.
     */
    @Builder.Default
    private List<ChatMessage> messages = new ArrayList<>();

    /**
     * the maximum number of rounds.
     */
    @Builder.Default
    private int maxRound = 10;

    /**
     * whether to allow the same speaker to speak consecutively.
     */
    @Builder.Default
    private boolean allowRepeatSpeaker = true;

    /**
     * Return the names of the agents in the group chat.
     *
     * @return a list of agent names
     */
    public List<String> agentNames() {
        return extractAgentNames(agents);
    }

    public GroupChat append(ChatMessage message) {
        messages.add(message);
        return this;
    }

    /**
     * Returns the agent with a given name.
     *