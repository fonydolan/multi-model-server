/*
 * Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file except in compliance
 * with the License. A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package com.amazonaws.ml.mms.archive;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public final class ZipUtils {

    private ZipUtils() {}

    public static void zip(File src, File dest, boolean includeRootDir) throws IOException {
        int prefix = src.getCanonicalPath().length();
        if (includeRootDir) {
            prefix -= src.getName().length();
        }
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(dest))) {
            addToZip(prefix, src, null, zos);
        }
    }

    public static void unzip(File src, File dest) throws IOException {
        unzip(new FileInputStream(src), dest);
    }

    public static void unzip(InputStream is, File dest) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(is)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                File file = new File(dest, name);
                if (entry.isDirectory()) {
                    FileUtils.forceMkdir(file);
                } else {
                    File parentFile = file.getParentFile();
                    FileUtils.forceMkdir(parentFile);
                    try (OutputStream os = new FileOutputStream(file)) {
                        IOUtils.copy(zis, os);
                    }
                }
            }
        }
    }

    public static void addToZip(int prefix, File file, FileFilter filter, ZipOutputStream zos)
            throws IOException {
        String name = file.getCanonicalPath().substring(prefix);
        if (name.startsWith(File.separator)) {
            name = name.substring(1);
        }
        if (file.isDirectory()) {
            if (!name.isEmpty()) {
                ZipEntry entry = new ZipEntry(name + File.separator);
                zos.putNextEntry(entry);
            }
            File[] files = file.listFiles(filter);
            if (files != null) {
                for (File f : files) {
                    addToZip(prefix, f, filter, zos);
                }
            }
        } else if (file.isFile()) {
            ZipEntry entry = new ZipEntry(name);
            zos.putNextEntry(entry);
            try (FileInputStream fis = new FileInputStream(file)) {
                IOUtils.copy(fis, zos);
            }
        }
    }
}
