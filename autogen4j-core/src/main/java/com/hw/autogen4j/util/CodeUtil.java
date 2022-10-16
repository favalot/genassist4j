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

package com.hw.autogen4j.util;

import com.hw.autogen4j.entity.CodeBlock;
import com.hw.autogen4j.entity.CodeExecutionConfig;
import com.hw.autogen4j.entity.CodeExecutionResult;
import com.hw.autogen4j.exception.Autogen4jException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.exec.*;
import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.hw.autogen4j.util.FileUtil.deleteFile;
import static com.hw.autogen4j.util.FileUtil.writeCodeToFile;

/**
 * @author HamaWhite
 */
public class CodeUtil {

    private CodeUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Matches multi-line code blocks.
     * <ul>
     * <li>[ \t]* - Matches the potential spaces before the language name.</li>
     * <li>(\w+)? - Matches the language, where the ? indicates it is optional.</li>
     * <li>[ \t]* - Matches the potential spaces (not newlines) after the language name.</li>
     * <li>\r?\n - Ensures there is a linebreak after ```.</li>
     * <li>(.*?) - Matches the code itself (non-greedy).</li>
     * <li>\r?\n - Ensures there is a linebreak before ```.</li>
     * <li>[ \t]* - Matches the potential spaces before the closing ``` (the spec allows indentation).</li>
     * </ul>
     */
    private static final String CODE_BLOCK_PATTERN = "```[ \\t]*(\\w+)?[ \\t]*\\r?\\n(.*?)\\r?\\n[ \\t]