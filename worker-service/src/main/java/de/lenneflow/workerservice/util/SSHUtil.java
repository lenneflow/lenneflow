package de.lenneflow.workerservice.util;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import de.lenneflow.workerservice.exception.InternalServiceException;
import io.fabric8.kubernetes.api.model.ListOptions;
import io.fabric8.kubernetes.client.*;

import java.io.ByteArrayOutputStream;

public class SSHUtil {

    public static Session createSSHSession(String host, int port, String username, String password) {
        Session session = null;
        try {
            session = new JSch().getSession(username,host,port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
        } catch (JSchException e) {
            throw new InternalServiceException("could not create SSH session to the host " + host);
        }
        return session;
    }

    public static void closeSSHSession(Session session){
        if (session != null) {
            session.disconnect();
        }
    }

    public static String executeSSHCommand(Session session, String command){
        String responseString;
        ChannelExec channel = null;
        try {
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(command);
            ByteArrayOutputStream responseStream = new ByteArrayOutputStream();
            channel.setOutputStream(responseStream);
            channel.connect(10000);

            responseString = new String(responseStream.toByteArray());
            return responseString;
        } catch (JSchException e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                channel.disconnect();
            }
        }
    }

    public static void main(String[] args) {
        Config config = new ConfigBuilder()
                .withMasterUrl("https://77.237.237.43:16443")
                .withTrustCerts(true)
                .withOauthToken("eyJhbGciOiJSUzI1NiIsImtpZCI6IlpfT1lYOGtqckNGV0lzNGRVY3oyXzRTYjFCeTdRMjY5Y3lKSUVtT1Q3T3MifQ.eyJhdWQiOlsiaHR0cHM6Ly9rdWJlcm5ldGVzLmRlZmF1bHQuc3ZjIl0sImV4cCI6MTc1NTUyMTQ2NiwiaWF0IjoxNzIzOTg1NDY2LCJpc3MiOiJodHRwczovL2t1YmVybmV0ZXMuZGVmYXVsdC5zdmMiLCJqdGkiOiJiZTQ5ZDZhOC00Y2JhLTQxN2ItODhkNi01MGQyN2ViYjM3MDIiLCJrdWJlcm5ldGVzLmlvIjp7Im5hbWVzcGFjZSI6ImRlZmF1bHQiLCJzZXJ2aWNlYWNjb3VudCI6eyJuYW1lIjoibGVubmVmbG93YXBpIiwidWlkIjoiMDhiYmU4OTYtZmQxNC00ZWYwLWEwZWMtNzY1NGUxMTAyZDY5In19LCJuYmYiOjE3MjM5ODU0NjYsInN1YiI6InN5c3RlbTpzZXJ2aWNlYWNjb3VudDpkZWZhdWx0Omxlbm5lZmxvd2FwaSJ9.l7knPZeIEYTFwVBLeALEfqrb_VkGzn3BEBFzCla_19I2TM3sQPLg2plKk9eDn7ZLj8y8C0zmXsydQJC0ETEFv6oSXebASP7LrlHjZrFwRALrZmVqB39fQrFOH1tnmicV2uzRoE2xL3VeqK2fAA8a0Os49emJ72043HsNDquJonFkucNFK66HJHeTiEg7yF_bqE6NeB3beXHcRXRMVxsgLrxRm3h3S2-mlzrDPPDBzwoxxA1iG9-i4tNmrKAbEmDu1XxOIYu4-e1c-QADSmQhJFXPHpIG12km2cEG3Is-W0FpylNE3EkLUuTuGFbMl20qbwfb_OnimBTJ4JxA5MhIYg")
                .build();
        KubernetesClient client = new KubernetesClientBuilder().withConfig(config).build();
        System.out.println(client.pods().inNamespace("lenneflow").list());
        client.close();
    }
}
