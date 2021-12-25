/*
 * Copyright 2021 The Eclipse foundation
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Apache License v2.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  The Apache License v2.0 is available at
 *  http://www.opensource.org/licenses/apache2.0.php
 *
 *  You may elect to redistribute this code under either of these licenses.
 */
package io.vertx.ext.web.handler;

/**
 * Enumaration of FileSystem access permissions, used in
 * 
 * @see "StaticHandler"
 * 
 * @author <a href="https://wissel.net">Stephan Wissel</a>
 */
public enum FileSystemAccess {

    /**
     * Access to the full file system, starting at "/",
     * Limited by the operating systems permission for
     * the user running the app. Use with care to avoid
     * indecent exposure
     */
    ROOT,

    /**
     * Access to files relative to the application's working
     * directory including the Java class path.
     * This is the recommended default option
     */
    RELATIVE
}
