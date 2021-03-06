/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.developerstudio.humantaskeditor.wizards;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.wso2.developerstudio.humantaskeditor.Activator;
import org.wso2.developerstudio.humantaskeditor.HumantaskEditorConstants;

public class FileManagementUtil {
    private static final Logger logger = Logger.getLogger(Activator.PLUGIN_ID);

    public static void copyDirectory(File srcPath, File dstPath, List filesToBeCopied) throws IOException {

        if (srcPath.isDirectory()) {
            if (!dstPath.exists()) {
                dstPath.mkdir();
            }
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                copyDirectory(new File(srcPath, files[i]), new File(dstPath, files[i]), filesToBeCopied);
            }
        } else {
            if (!filesToBeCopied.contains(srcPath.getAbsolutePath()))
                return;
            if (!srcPath.exists()) {
                return;
            } else {
                FileManagementUtil.copy(srcPath, dstPath);
            }
        }
    }

    public static List getAllFilesPresentInFolder(File srcPath) {
        List fileList = new ArrayList();
        if (srcPath.isDirectory()) {
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                fileList.addAll(getAllFilesPresentInFolder(new File(srcPath, files[i])));
            }
        } else {
            fileList.add(srcPath.getAbsolutePath());
        }
        return fileList;
    }

    public static void removeEmptyDirectories(File srcPath) {
        if (srcPath.isDirectory()) {
            String files[] = srcPath.list();
            for (int i = 0; i < files.length; i++) {
                removeEmptyDirectories(new File(srcPath, files[i]));
            }
            if (srcPath.list().length == 0) {
                srcPath.delete();
            }
        }
    }

    static public void zipFolder(String srcFolder, String destZipFile) throws Exception {
        ZipOutputStream zip = null;
        FileOutputStream fileWriter = null;
        try {
            fileWriter = new FileOutputStream(destZipFile);
            zip = new ZipOutputStream(fileWriter);
        } catch (FileNotFoundException e) {
            logger.log(Level.FINE, HumantaskEditorConstants.ERROR_CREATING_CORRESPONDING_ZIP_FILE, e);
        }
        addFolderContentsToZip(srcFolder, zip);
        try {
            zip.flush();
            zip.close();
        } catch (IOException ex) {
            logger.log(Level.FINE, HumantaskEditorConstants.ERROR_CREATING_CORRESPONDING_ZIP_FILE, ex);
        }
    }

    static private void addToZip(String path, String srcFile, ZipOutputStream zip) {

        File folder = new File(srcFile);

        if (folder.isDirectory()) {
            addFolderToZip(path, srcFile, zip);
        } else {
            // Transfer bytes from in to out
            if (!srcFile.equals(".project")) {
                byte[] buf = new byte[1024];
                int len;
                try {
                    FileInputStream in = new FileInputStream(srcFile);
                    String location = folder.getName();
                    if (!path.equalsIgnoreCase("")) {
                        location = path + File.separator + folder.getName();
                    }
                    zip.putNextEntry(new ZipEntry(location));
                    while ((len = in.read(buf)) > 0) {
                        zip.write(buf, 0, len);
                    }
                    in.close();
                } catch (FileNotFoundException ex) {
                    logger.log(Level.FINE, HumantaskEditorConstants.ERROR_CREATING_CORRESPONDING_ZIP_FILE, ex);
                } catch (IOException e) {
                    logger.log(Level.FINE, HumantaskEditorConstants.ERROR_CREATING_CORRESPONDING_ZIP_FILE, e);
                }
            }
        }
    }

    static private void addFolderContentsToZip(String srcFolder, ZipOutputStream zip) {
        File folder = new File(srcFolder);
        String fileListe[] = folder.list();
        int i = 0;
        while (i < fileListe.length) {
            addToZip("", srcFolder + File.separator + fileListe[i], zip);
            i++;
        }
    }

    static private void addFolderToZip(String path, String srcFolder, ZipOutputStream zip) {
        File folder = new File(srcFolder);
        String fileListe[] = folder.list();
        int i = 0;
        while (true) {
            String newPath = folder.getName();
            if (!path.equalsIgnoreCase("")) {
                newPath = path + File.separator + newPath;
            }
            addToZip(newPath, srcFolder + File.separator + fileListe[i], zip);
            i++;
        }

    }

    public static void copyFile(String src, String dest) {
        InputStream is = null;
        FileOutputStream fos = null;

        try {
            is = new FileInputStream(src);
            fos = new FileOutputStream(dest);
            int c = 0;
            byte[] array = new byte[1024];
            while ((c = is.read(array)) >= 0) {
                fos.write(array, 0, c);
            }
        } catch (FileNotFoundException e) {
            logger.log(Level.FINE, HumantaskEditorConstants.ERROR_COPYING_FILES, e);
        } catch (IOException e) {
            logger.log(Level.FINE, HumantaskEditorConstants.ERROR_COPYING_FILES, e);
        } finally {
            try {
                fos.close();
                is.close();
            } catch (IOException e) {
                logger.log(Level.FINE, HumantaskEditorConstants.ERROR_COPYING_FILES, e);
            }
        }
    }

    public static File createFileAndParentDirectories(String fileName) throws IOException {
        File file = new File(fileName);
        File parent = file.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        file.createNewFile();
        return file;
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    public static void deleteDirectories(File dir) {
        File[] children = dir.listFiles();
        for (int i = 0; i < children.length; i++) {
            if (children[i].list() != null && children[i].list().length > 0) {
                deleteDirectories(children[i]);
            } else {
                children[i].delete();
            }
        }
        dir.delete();
    }

    public static void deleteDirectories(String dir) {
        File directory = new File(dir);
        deleteDirectories(directory);
    }

    public static void createTargetFile(String sourceFileName, String targetFileName) throws IOException {
        createTargetFile(sourceFileName, targetFileName, false);
    }

    public static void createTargetFile(String sourceFileName, String targetFileName, boolean overwrite)
            throws IOException {
        File idealResultFile = new File(targetFileName);
        if (overwrite || !idealResultFile.exists()) {
            FileManagementUtil.createFileAndParentDirectories(targetFileName);
            FileManagementUtil.copyFile(sourceFileName, targetFileName);
        }
    }

    public static boolean createDirectory(String directory) {
        // Create a directory; all ancestor directories must exist
        boolean success = (new File(directory)).mkdir();
        if (!success) {
            // Directory creation failed
        }
        return success;
    }

    public static boolean createDirectorys(String directory) {
        // Create a directory; all ancestor directories must exist
        boolean success = (new File(directory)).mkdirs();
        if (!success) {
            // Directory creation failed
        }
        return success;
    }

    // Copies all files under srcDir to dstDir.
    // If dstDir does not exist, it will be created.
    public static void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdirs();
            }

            String[] children = srcDir.list();
            for (int i = 0; i < children.length; i++) {
                copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            copy(srcDir, dstDir);
        }
    }

    // Copies src file to dst file.
    // If the dst file does not exist, it is created
    public static void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    public static String addAnotherNodeToPath(String currentPath, String newNode) {
        return currentPath + File.separator + newNode;
    }

    public static String addNodesToPath(String currentPath, String[] newNode) {
        StringBuilder builder = new StringBuilder();
        builder.append(currentPath);
        for (int i = 0; i < newNode.length; i++) {
            builder.append(File.separator).append(newNode[i]);
        }
        return builder.toString();
    }

    public static String addNodesToPath(StringBuffer currentPath, String[] pathNodes) {
        for (int i = 0; i < pathNodes.length; i++) {
            currentPath.append(File.separator);
            currentPath.append(pathNodes[i]);
        }
        return currentPath.toString();
    }

    public static String addNodesToURL(String currentPath, String[] newNode) {
        StringBuilder builder = new StringBuilder();
        builder.append(currentPath);
        for (int i = 0; i < newNode.length; i++) {
            builder.append("/").append(newNode[i]);
        }
        return builder.toString();
    }

    /**
     * Get the list of file with a prefix of <code>fileNamePrefix</code> &amp; an extension of <code>extension</code>
     *
     * @param sourceDir The directory in which to search the files
     * @param fileNamePrefix The prefix to look for
     * @param extension The extension to look for
     * @return The list of file with a prefix of <code>fileNamePrefix</code> &amp; an extension of
     *         <code>extension</code>
     */
    public static File[] getMatchingFiles(String sourceDir, String fileNamePrefix, String extension) {
        List fileList = new ArrayList();
        File libDir = new File(sourceDir);
        String libDirPath = libDir.getAbsolutePath();
        String[] items = libDir.list();
        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                String item = items[i];
                if (fileNamePrefix != null && extension != null) {
                    if (item.startsWith(fileNamePrefix) && item.endsWith(extension)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else if (fileNamePrefix == null && extension != null) {
                    if (item.endsWith(extension)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else if (fileNamePrefix != null && extension == null) {
                    if (item.startsWith(fileNamePrefix)) {
                        fileList.add(new File(libDirPath + File.separator + item));
                    }
                } else {
                    fileList.add(new File(libDirPath + File.separator + item));
                }
            }
            return (File[]) fileList.toArray(new File[fileList.size()]);
        }
        return new File[0];
    }

    /**
     * Filter out files inside a <code>sourceDir</code> with matching <codefileNamePrefix></code>
     * and <code>extension</code>
     * 
     * @param sourceDir The directory to filter the files
     * @param fileNamePrefix The filtering filename prefix
     * @param extension The filtering file extension
     */
    public static void filterOutRestrictedFiles(String sourceDir, String fileNamePrefix, String extension) {
        File[] resultedMatchingFiles = getMatchingFiles(sourceDir, fileNamePrefix, extension);
        for (int i = 0; i < resultedMatchingFiles.length; i++) {
            File matchingFilePath = new File(resultedMatchingFiles[i].getAbsolutePath());
            matchingFilePath.delete();
        }
    }

    /**
     * Returns the immediate directories within the <code>sourceDir</code>
     * 
     * @param sourceDir : source directory to check the directories
     * @return List of the Directories
     */
    public static List<IPath> getDirsOnly(String sourceDir) {
        List<IPath> resultList = new ArrayList<IPath>();
        File srcDir = new File(sourceDir);
        if (srcDir.exists()) {
            File[] listOfFilesAndDirs = srcDir.listFiles();
            for (int i = 0; i < listOfFilesAndDirs.length; i++) {
                Path pathInstance = new Path(listOfFilesAndDirs[i].getAbsolutePath());
                if (listOfFilesAndDirs[i].getAbsolutePath() != null
                        && new File(listOfFilesAndDirs[i].getAbsolutePath()).isDirectory()) {
                    IPath pathWithOutLastSegment = pathInstance.removeLastSegments(1);
                    if (pathWithOutLastSegment != null && pathWithOutLastSegment.toOSString().equals(sourceDir)) {
                        resultList.add(pathInstance);
                    }
                }
            }
        }
        return resultList;
    }
}
