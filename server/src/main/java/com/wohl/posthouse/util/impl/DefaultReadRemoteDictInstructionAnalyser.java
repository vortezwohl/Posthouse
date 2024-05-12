package com.wohl.posthouse.util.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wohl.posthouse.context.JentitiContext;
import com.wohl.posthouse.util.*;
import com.wohl.posthouse.util.intf.ReadRemoteDictInstructionAnalyser;
import com.wohl.posthouse.util.intf.RemoteDictRead;
import org.jentiti.annotation.Singleton;

@Singleton("readRemoteDictInstructionAnalyser")
public class DefaultReadRemoteDictInstructionAnalyser implements ReadRemoteDictInstructionAnalyser {
    @Override
    public String exec(String instruction) throws JsonProcessingException {

        // timestamp ttl(s) read {k}

        RemoteDictRead remoteDictRead = (RemoteDictRead) JentitiContext.ctx().get("remoteDictRead");

        String[] instructionTokens = instruction.split(Delimiter.getBodyStart(),2)[1].split(Delimiter.getBodyEnd(),2)[0].split(Delimiter.get(),4);

        String resp;

        switch (instructionTokens[2]) {
            case "RKS":
            case "rks":
                resp = remoteDictRead.readKeys();
                break;
            case "RKES":
            case "rkes":
                resp = remoteDictRead.readKeyExp();
                break;
            case "RK":
            case "rk":
                resp = remoteDictRead.readKey(instructionTokens[3]);
                break;
            default:
                resp = null;
                break;
        }
        return resp;
    }

    @Override
    public boolean isReadReq(String request) {
        String[] instructionTokens = request.split(Delimiter.getBodyStart(),2)[1].split(Delimiter.getBodyEnd(),2)[0].split(Delimiter.get(),3);
        return instructionTokens[2].startsWith("R") || instructionTokens[2].startsWith("r");
    }
}
