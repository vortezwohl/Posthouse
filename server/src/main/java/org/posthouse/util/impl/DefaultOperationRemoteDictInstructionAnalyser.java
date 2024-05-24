package org.posthouse.util.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jentiti.annotation.Singleton;
import org.posthouse.context.JentitiContext;
import org.posthouse.store.RemoteDictStore;
import org.posthouse.util.Delimiter;
import org.posthouse.util.intf.RemoteDictCreate;
import org.posthouse.util.intf.RemoteDictInstructionAnalyser;
import org.posthouse.util.intf.RemoteDictModify;
import org.posthouse.util.intf.RemoteDictRemove;

import java.util.Deque;
import java.util.Map;
import java.util.Set;

@Singleton("remoteDictInstructionAnalyser")
public class DefaultOperationRemoteDictInstructionAnalyser implements RemoteDictInstructionAnalyser {
    @Override
    public boolean exec(String instruction) throws JsonProcessingException {

        // timestamp ttl(s) op k {v}
        //-1 means persist

        ObjectMapper objectMapper = new ObjectMapper();

        RemoteDictCreate remoteDictCreate = (RemoteDictCreate) JentitiContext.ctx().get("remoteDictCreate");
        RemoteDictModify remoteDictModify = (RemoteDictModify) JentitiContext.ctx().get("remoteDictModify");
        RemoteDictRemove remoteDictRemove = (RemoteDictRemove) JentitiContext.ctx().get("remoteDictRemove");

        String[] instructionTokens = instruction.split(Delimiter.getBodyStart(),2)[1].split(Delimiter.getBodyEnd(),2)[0].split(Delimiter.get(),5);

        long ttl = Long.parseLong(instructionTokens[1]);
        long expirationTime = Long.parseLong(instructionTokens[0]) + ttl * 1000;
        boolean keyNotExist = !RemoteDictStore.exist(instructionTokens[3]);
        boolean resp = false;

        // 增删改 协议解析
        switch(instructionTokens[2]) {

            // create
            case "CST":
            case "cst":
                // create string
                //timestamp ttl(s) CST k v
                if(remoteDictCreate.createString(instructionTokens[3], instructionTokens[4]) != null) {
                    if(ttl >= 0)
                        RemoteDictStore.addExpKey(instructionTokens[3], expirationTime);
                    resp = true;
                }
                break;
            case "CHA":
            case "cha":
                // create hash map
                // timestamp ttl(s) CHA k
                if(remoteDictCreate.createMap(instructionTokens[3]) != null) {
                    if(ttl >= 0)
                        RemoteDictStore.addExpKey(instructionTokens[3], expirationTime);
                    resp = true;
                }
                break;
            case "CHAI":
            case "chai":
                // create hash map
                // timestamp ttl(s) CHAI k f:v
                String[] entry = instructionTokens[4].split(":");
                String field = entry[0];
                String value = entry[1];
                if(remoteDictCreate.createMapEntry(instructionTokens[3], field, value) != null) {
                    if(keyNotExist && ttl >= 0)
                        RemoteDictStore.addExpKey(instructionTokens[3], expirationTime);
                    resp = true;
                }
                break;
            case "CDE":
            case "cde":
                // create deque
                // timestamp ttl(s) CDE k
                if(remoteDictCreate.createDeque(instructionTokens[3]) != null) {
                    if(ttl >= 0)
                        RemoteDictStore.addExpKey(instructionTokens[3], expirationTime);
                    resp = true;
                }
                break;
            case "CDEI":
            case "cdei":
                // create deque
                // timestamp ttl(s) CDEI k v
                if(remoteDictCreate.createDequeItem(instructionTokens[3], instructionTokens[4]) != null) {
                    if(keyNotExist && ttl >= 0)
                        RemoteDictStore.addExpKey(instructionTokens[3], expirationTime);
                    resp = true;
                }
                break;
            case "CSE":
            case "cse":
                // create set
                // timestamp ttl(s) CSE k
                if(remoteDictCreate.createSet(instructionTokens[3]) != null) {
                    if(ttl >= 0)
                        RemoteDictStore.addExpKey(instructionTokens[3], expirationTime);
                    resp = true;
                }
                break;
            case "CSEI":
            case "csei":
                // create set item
                // timestamp ttl(s) CSEI k v
                if(remoteDictCreate.createSetItem(instructionTokens[3], instructionTokens[4]) != null) {
                    if(keyNotExist && ttl >= 0)
                        RemoteDictStore.addExpKey(instructionTokens[3], expirationTime);
                    resp = true;
                }
                break;

            // modify
            case "MST":
            case "mst":
                // modify string
                // timestamp ttl(s) MST k v
                resp = remoteDictModify.modifyString(instructionTokens[3], instructionTokens[4]);
                break;
            case "MHA":
            case "mha":
                // modify string
                // timestamp ttl(s) MHA k v
                Map map = objectMapper.readValue(instructionTokens[4], Map.class);
                resp = remoteDictModify.modifyMap(instructionTokens[3], map);
                break;
            case "MDE":
            case "mde":
                // modify string
                // timestamp ttl(s) MDE k v
                Deque deque = objectMapper.readValue(instructionTokens[4], Deque.class);
                resp = remoteDictModify.modifyDeque(instructionTokens[3], deque);
                break;
            case "MSE":
            case "mse":
                // modify string
                // timestamp ttl(s) MSE k v
                Set set = objectMapper.readValue(instructionTokens[4], Set.class);
                resp = remoteDictModify.modifySet(instructionTokens[3], set);
                break;
            case "EXP":
            case "exp":
                // modify ttl
                // timestamp ttl(s) EXP k
                if(ttl >= 0)
                    resp = remoteDictModify.modifyTTL(instructionTokens[3], expirationTime);
                else
                    resp = RemoteDictStore.removeExpKey(instructionTokens[3]);
                break;
            default:
                resp = false;
                break;

            // delete
            case "DHAI":
            case "dhai":
                // remove hash item
                // timestamp ttl(s) RHAI k field
                resp = remoteDictRemove.removeHashItem(instructionTokens[3],instructionTokens[4]);
                break;
            case "DDEI":
            case "ddei":
                // remove deque item
                // timestamp ttl(s) RDEI k value
                resp = remoteDictRemove.removeDequeItem(instructionTokens[3], instructionTokens[4]);
                break;
            case "DSEI":
            case "dsei":
                // remove set item
                // timestamp ttl(s) RSEI k value
                resp = remoteDictRemove.removeSetItem(instructionTokens[3], instructionTokens[4]);
            case "DK":
            case "dk":
                resp = remoteDictRemove.removeKey(instructionTokens[3]);
        }
        return resp;
    }
}
