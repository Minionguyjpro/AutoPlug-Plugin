/*
 * Copyright Osiris Team
 * All rights reserved.
 *
 * This software is copyrighted work licensed under the terms of the
 * AutoPlug License.  Please consult the file "LICENSE" for details.
 */

package com.osiris.autoplug.plugin;

import com.osiris.dyml.DreamYaml;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;

import static com.osiris.autoplug.plugin.Constants.CON;
import static com.osiris.autoplug.plugin.Constants.LOG;

/**
 * Communicates with the local-client and passes over the plugin information
 */
public class AutoPlugClientConnection {
    private static BufferedWriter bw;
    private int port;
    private String key;
    private Socket socket;

    public AutoPlugClientConnection() throws Exception {
        DreamYaml config = new DreamYaml(System.getProperty("user.dir") + "/autoplug/system/config.yml");
        if (!config.getFile().exists())
            throw new RuntimeException("File '" + config.getFile() + "' is missing! Please make sure that the AutoPlug-Client was installed and setup correctly.");

        try {
            config.load();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        key = config.get("config", "autoplug-plugin-key").asString();
        port = config.get("config", "autoplug-plugin-port").asInt();

        LOG.info("Connecting AutoPlug-Client on localhost:"+port+" ...");
        socket = new Socket("localhost", port);
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        LOG.info("Connected to AutoPlug-Client! Comparing plugin keys...");

        // Send server key to AutoPlug-Client on that port and let it be compared
        if (dis.readUTF().equals(key)) {
            dos.writeUTF(key);
            socket.setSoTimeout(0);
            bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            LOG.info("Private plugin keys match. Connection established.");
        } else
            throw new Exception("Private local keys don't match!");
    }

    public static synchronized void send(@NotNull String message) {
        try {
            if (bw != null) {
                if (!message.endsWith(System.lineSeparator())) {
                    bw.write(message + System.lineSeparator());
                } else {
                    bw.write(message);
                }
                bw.flush();
            }
        } catch (Exception e) { // Do not use AL.warn because that would cause an infinite loop
        }
    }

    public Socket getSocket() {
        return socket;
    }

}
