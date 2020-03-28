package com.example.myapp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class Gateway {

    private ProcessBuilder pb;
    private List<String> commandList;

    public Gateway(){
            pb = new ProcessBuilder();
    }

    //Fce pro spuštění příkazu
    public List<String> runCommand(String command){
        commandList = parseStringArguments(command);
        pb.command(commandList);

        List<String> result = new ArrayList<String>();

        try {
            Process process = pb.start();


            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                result.add(line + "\n");
            }

            int exit = process.waitFor();
            if (exit == 0) {
                Log.d("gate", "Success!");
                Log.d("gate", result.toString());
                return result;
            } else if (exit == 127){
                Log.d("gate", "neexistující příkaz");
            }
            else {
                Log.d("gate", "weird");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result;
    }

    //String parser pro process builder
    public List<String> parseStringArguments(String request){
        //Vzor - GET /cgi-bin/cat/proc/cpuinfo HTTP/1.1
        List<String> tmpStringList = new ArrayList<String>();
        String substring;

        substring = request.replace("GET /cgi-bin/", "");
        if(substring.contains(" "))
            substring = substring.substring(0, substring.lastIndexOf(" "));

        String cmd = substring;
        String arg = "";

        if(substring.contains("/")) {
            cmd = substring.substring(0, substring.indexOf("/"));
            arg = substring.replace(cmd, "");
        }

        //tmpStringList.add("sh");
        //tmpStringList.add("-c");

        tmpStringList.add(cmd);

        if(arg.isEmpty());
            tmpStringList.add(arg);

        return tmpStringList;
    }
}
