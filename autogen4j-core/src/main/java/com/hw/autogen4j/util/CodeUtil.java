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
    private static final String CODE_BLOCK_PATTERN = "```[ \\t]*(\\w+)?[ \\t]*\\r?\\n(.*?)\\r?\\n[ \\t]*```";

    /**
     * Extract code from a text.
     *
     * @param text the content to extract code from.
     * @return a list of code blocks, each containing the language and the code.
     */
    public static List<CodeBlock> extractCode(String text) {
        return extractCode(text, false);
    }

    /**
     * Extract code from a text.
     *
     * @param text                 the content to extract code from.
     * @param detectSingleLineCode extracting single line code.
     * @return a list of code blocks, each containing the language and the code.
     */
    public static List<CodeBlock> extractCode(String text, boolean detectSingleLineCode) {
        List<CodeBlock> extracted = new ArrayList<>();
        if (!detectSingleLineCode) {
            Matcher matcher = Pattern.compile(CODE_BLOCK_PATTERN, Pattern.DOTALL).matcher(text);
            while (matcher.find()) {
                extracted.add(new CodeBlock(matcher.group(1), matcher.group(2)));
            }
            return extracted;
        }

        // for extracting multi-line and single-line code block, `([^`]+)` matches inline code.
        Matcher matcher = Pattern.compile(CODE_BLOCK_PATTERN + "|`([^`]+)`").matcher(text);
        // extract the individual code blocks and languages from the matched groups
        while (match