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

package com.hw.autogen4j.example;

import com.hw.autogen4j.agent.AssistantAgent;
import com.hw.autogen4j.agent.UserProxyAgent;
import com.hw.autogen4j.agent.group.GroupChat;
import com.hw.autogen4j.agent.group.GroupChatManager;
import com.hw.autogen4j.entity.CodeExecutionConfig;

import java.util.List;

import static com.hw.autogen4j.entity.HumanInputMode.TERMINATE;

/**
 * AutoGen offers conversable agents powered by LLM, tool or human, which can be used to perform tasks collectively
 * via automated chat. This framework allows tool use and human participation through multi-agent conversation.
 * <p>
 * <a href="https://github.com/microsoft/autogen/blob/main/notebook/agentchat_groupchat.ipynb">agentchat_groupchat</a>
 *
 * @author HamaWhite
 */
public class GroupChatExample {

    public static void main(String[] args) {

        var codeExecutionConfig = CodeExecutionConfig.builder()
                .workDir("data/group_chat")
                .lastMessagesNumber(2)
                .build();

        // create a 