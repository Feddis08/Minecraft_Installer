package at.feddis08;

import org.json.JSONObject;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Objects;

public class Main {

    static JSONObject config = new JSONObject();


    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Hello world!");

        File f = new File("./build.conf");
        if (f.exists()){
            config = new JSONObject(getFile(f.getPath()).get(0));
            if (config.getString("minecraft_eula").equals("false")){
                System.out.println("Set the Minecraft EULA to 'true'");
            }else if(config.getString("minecraft_eula").equals("true")){

                if (config.getBoolean("server_finished")){

                    System.out.println("Starting Server...");
                    Thread t = new Thread(){

                        @Override
                        public void run(){
                            try {
                                start_server();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    };
                    t.start();

                }else{

                    System.out.println("Creating Minecraft Server");
                    create_server(config);

                    System.out.println("Editing EULA");
                    edit_eula();

                    System.out.println("Starting Server...");
                    Thread t = new Thread(){

                        @Override
                                public void run(){
                            try {
                                start_server();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }

                        }
                    };
                    t.start();

                    config.put("server_finished", true);

                    f = new File("./build.conf");
                    f.delete();
                    FileWriter fileWriter = new FileWriter(f);
                    System.out.println(config.toString());
                    fileWriter.write(config.toString());
                    fileWriter.close();
                }
            }
        }else{
            config = create_config();
            FileWriter fileWriter = new FileWriter(f);
            System.out.println(config.toString());
            fileWriter.write(config.toString());
            fileWriter.close();
            System.out.println("Config file was created.");
            System.out.println("Set the Minecraft EULA to 'true'");
        }
    }

    public static JSONObject create_config(){
        JSONObject config = new JSONObject();
        config.put("folder_name", "./minecraft");
        config.put("link_to_engine", "https://api.papermc.io/v2/projects/paper/versions/1.19.4/builds/540/downloads/paper-1.19.4-540.jar");
        config.put("path_to_java", "java");
        config.put("minecraft_eula", "false");
        config.put("install_java", "false");
        config.put("server_finished", false);

        return config;
    }
    public static ArrayList<String> getFile(String path) throws IOException {
        File file = new File(path);
        BufferedReader br = new BufferedReader(new FileReader(file));
        String st;
        ArrayList<String> st1 = new ArrayList<>();
        while ((st = br.readLine()) != null) Objects.requireNonNull(st1).add(st);
        return st1;
    }
    public static void get_from_url(String url, String file_name, String path) throws IOException {
        System.out.println("Downloading file: " + url + " " + path + "/" + file_name);
        InputStream in = new URL(url).openStream();
        Files.copy(in, Paths.get(path +"/"+ file_name), StandardCopyOption.REPLACE_EXISTING);
    }
    public static File create_setup_script(JSONObject config) throws IOException {
        File tempScript = File.createTempFile("script", null);

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                tempScript));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        printWriter.println("#!/bin/bash");
        printWriter.println("apt-get update -y");
        printWriter.println("apt-get upgrade -y");
        if (!config.getString("install_java").equals("false")){
            printWriter.println("apt-get install openjdk-" + config.getString("install_java") + "-jre -y");
        }

        printWriter.println("cd " + config.getString("folder_name"));
        printWriter.println(config.getString("path_to_java") + " -jar engine.jar");
        printWriter.close();

        return tempScript;
    }
    public static File create_start_script(JSONObject config) throws IOException {
        File tempScript = File.createTempFile("script", null);

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                tempScript));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        printWriter.println("#!/bin/bash");
        printWriter.println("apt-get update -y");
        printWriter.println("apt-get upgrade -y");
        printWriter.println("cd " + config.getString("folder_name"));
        printWriter.println(config.getString("path_to_java") + " -jar engine.jar");
        printWriter.close();

        return tempScript;
    }

    public static void create_server(JSONObject config) throws IOException, InterruptedException {

        System.out.println("Writing in folder'" + config.getString("folder_name") + "'.");
        File f = new File(config.getString("folder_name"));
        f.mkdir();
        System.out.println("Downloading server engine from: '" + config.getString("link_to_engine") + "'.");
        get_from_url(config.getString("link_to_engine"), "engine.jar", f.getPath());

        System.out.println("Starting setup...");
        File tempScript = create_setup_script(config);

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } finally {
            tempScript.delete();
        }

    }

    public static void edit_eula() throws IOException {


        File f = new File(config.getString("folder_name") + "/eula.txt");
        f.delete();
        FileWriter fileWriter = new FileWriter(f);
        fileWriter.write("eula=true");
        fileWriter.close();
    }

    public static void start_server() throws IOException, InterruptedException {
        File tempScript = create_start_script(config);

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            pb.inheritIO();
            Process process = pb.start();
            process.waitFor();
        } finally {
            tempScript.delete();
        }

    }

}