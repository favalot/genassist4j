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

import static com.hw.autogen4j.entity.HumanInputMode.NEVER;

/**
 * Assistant agent, designed to solve a task with LLM.
 *
 * @author HamaWhite
 */
public class AssistantAgent extends ConversableAgent {

    private static final String DEFAULT_SYSTEM_MESSAGE =
            """
                    You are a helpful AI assistant.
                    Solve tasks using your coding and language skills.
                    In the following cases, suggest python code (in a python coding block) or shell script (in a sh coding block) for the user to execute.
                        1. When you need to collect info, use the code to output the info you need, for example, browse or search the web, download/read a file, print the content of a webpage or a file, get the current date/time, check the operating system. After sufficient info is printed and the task is ready to be solved based on your language skill, you can solve the task by yourself.
                        2. When you need to perform some task with code, use the code to perform the task and output the result. Finish the task smartly.
                    Solve the task step by step if you need to. If a plan is not provided, explain your plan first. Be clear which step uses code