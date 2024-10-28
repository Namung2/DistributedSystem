package distributed.tcpserver1;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TcpServer1 {
    private final String loadBalancerHost;
    private final int loadBalancerPort;
    private int serverPort; // 사용자 입력 포트 저장

    public TcpServer1(String loadBalancerHost, int loadBalancerPort) {
        this.loadBalancerHost = loadBalancerHost;
        this.loadBalancerPort = loadBalancerPort;
    }

    public void startConsole() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the port number for this server:");
        serverPort = Integer.parseInt(scanner.nextLine().trim());

        // 헬스 체크 서버 시작
        TcpHealthCheckServer healthCheckServer = new TcpHealthCheckServer(serverPort);
        healthCheckServer.start();

        System.out.println("Enter 1 to register or 2 to unregister the server. Type 'exit' to quit.");

        while (true) {
            System.out.print("> ");
            String input = scanner.nextLine().trim();

            if ("exit".equalsIgnoreCase(input)) {
                System.out.println("Exiting...");
                break;
            } else if ("1".equals(input)) {
                String jsonRequest = String.format(
                        "{\"cmd\":\"register\",\"protocol\":\"tcp\",\"port\":%d}", serverPort);
                String response = sendHttpRequest(jsonRequest);
                System.out.println("Response from LoadBalancer: " + response);
            } else if ("2".equals(input)) {
                String jsonRequest = String.format(
                        "{\"cmd\":\"unregister\",\"protocol\":\"tcp\",\"port\":%d}", serverPort);
                String response = sendHttpRequest(jsonRequest);
                System.out.println("Response from LoadBalancer: " + response);
            } else {
                System.out.println("Invalid input. Please enter '1' to register, '2' to unregister, or 'exit' to quit.");
            }
        }

        scanner.close();
    }

    private String sendHttpRequest(String jsonRequest) {
        try (Socket socket = new Socket(loadBalancerHost, loadBalancerPort);
             OutputStream outputStream = socket.getOutputStream();
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(outputStream, true)) {

            // HTTP 요청 작성
            String httpRequest = "POST /loadbalancer/register HTTP/1.1\r\n" +
                    "Host: " + loadBalancerHost + ":" + loadBalancerPort + "\r\n" +
                    "Content-Type: application/json\r\n" +
                    "Content-Length: " + jsonRequest.length() + "\r\n" +
                    "\r\n" +
                    jsonRequest;

            // 요청 전송
            out.print(httpRequest);
            out.flush();

            // 응답 헤더 및 바디 파싱
            StringBuilder headers = new StringBuilder();
            StringBuilder responseBody = new StringBuilder();
            String responseLine;
            int contentLength = 0;
            boolean isHeaderParsed = false;

            // 헤더 파싱
            while ((responseLine = in.readLine()) != null) {
                if (responseLine.isEmpty()) {
                    isHeaderParsed = true;
                    break;
                }
                headers.append(responseLine).append("\n");

                if (responseLine.toLowerCase().startsWith("content-length:")) {
                    contentLength = Integer.parseInt(responseLine.split(":")[1].trim());
                }
            }

            // 바디 파싱
            if (isHeaderParsed && contentLength > 0) {
                char[] buffer = new char[contentLength];
                int bytesRead = in.read(buffer, 0, contentLength);
                if (bytesRead > 0) {
                    responseBody.append(buffer, 0, bytesRead);
                }
            }

            return responseBody.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"ack\":\"failed\",\"msg\":\"Failed to communicate with LoadBalancer\"}";
        }
    }

}
