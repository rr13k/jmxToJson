package com.company.jmxToJSON;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newFor("JmxToJSON").build()
                .version(about.__version__)
                .description(about.__description__);

        parser.addArgument("-V","-v","--version")
                .dest("version")
                .action(Arguments.version())
                .help("show version");

        parser.addArgument("jmx_source_file")
                .nargs("+")
                .help("Specify jmx source file");

        parser.addArgument("-2y", "--to-yml", "--to-yaml")
                .dest("to_yaml")
                .action(Arguments.storeTrue())
                .help("Convert to YAML format, if not specified, convert to JSON format by default.");
        try {
            Namespace res = parser.parseArgs(args);
            String output_file_type = res.get("to_yaml")  ? "YML" : "JSON";
            ArrayList jmx_source_file  = res.get("jmx_source_file");
            for(  Object file : jmx_source_file){
                String fileName = file.toString();
                System.out.println(fileName);
                new jmxParser(file.toString(), output_file_type).Start();
            }
        } catch (ArgumentParserException e) {
            parser.handleError(e);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
