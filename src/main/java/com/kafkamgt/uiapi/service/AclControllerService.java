package com.kafkamgt.uiapi.service;


import com.kafkamgt.uiapi.config.ManageDatabase;
import com.kafkamgt.uiapi.dao.Acl;
import com.kafkamgt.uiapi.dao.AclRequests;
import com.kafkamgt.uiapi.dao.Env;
import com.kafkamgt.uiapi.error.KafkawizeException;
import com.kafkamgt.uiapi.helpers.HandleDbRequests;
import com.kafkamgt.uiapi.model.AclInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class AclControllerService {

    private HandleDbRequests handleDbRequests = ManageDatabase.handleDbRequests;

    @Autowired
    private UtilService utilService;

    @Autowired
    ClusterApiService clusterApiService;

    public AclControllerService(ClusterApiService clusterApiService, UtilService utilService){
        this.clusterApiService = clusterApiService;
        this.utilService = utilService;
    }

    public String createAcl(AclRequests aclReq) {

        aclReq.setUsername(utilService.getUserName());

        String execRes = handleDbRequests.requestForAcl(aclReq);
        return "{\"result\":\""+execRes+"\"}";
    }

    public String updateSyncAcls(String updateSyncAcls, String envSelected) {

        if(!utilService.checkAuthorizedSU())
            return "{\"result\":\"Not Authorized\"}";

        StringTokenizer strTkr = null;
        if(updateSyncAcls != null)
            strTkr = new StringTokenizer(updateSyncAcls,"\n");

        String topicSel, teamSelected, consumerGroup, aclIp, aclSsl, aclType, tmpToken;
        List<Acl> listTopics = new ArrayList<>();
        Acl t;

        StringTokenizer strTkrIn ;
        while(strTkr != null && strTkr.hasMoreTokens()){
            tmpToken = strTkr.nextToken().trim();
            strTkrIn = new StringTokenizer(tmpToken,"-----");
            while(strTkrIn.hasMoreTokens()){
                t = new Acl();

                topicSel = strTkrIn.nextToken();
                teamSelected = strTkrIn.nextToken();
                consumerGroup = strTkrIn.nextToken();
                aclIp =strTkrIn.nextToken();
                aclSsl = strTkrIn.nextToken();
                aclType = strTkrIn.nextToken();

                t.setTopicname(topicSel);
                t.setConsumergroup(consumerGroup);
                t.setAclip(aclIp);
                t.setAclssl(aclSsl);
                t.setTeamname(teamSelected);
                t.setEnvironment(envSelected);
                t.setTopictype(aclType);

                listTopics.add(t);
            }
        }

        try{
            if(listTopics.size()>0){
                String deleteResult = handleDbRequests.deletePrevAclRecs(listTopics);

                if(deleteResult.equals("success")) {
                    return "{\"result\":\""+handleDbRequests.addToSyncacls(listTopics)+"\"}";
                }
            }
            else
                return "{\"result\":\"No records to update\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
        return "{\"result\":\"failure\"}";
    }

    public List<AclRequests> getAclRequests() {
        return handleDbRequests.getAllAclRequests(utilService.getUserName());
    }

    public List<List<AclRequests>> getCreatedAclRequests() {

        List<List<AclRequests>> updatedAclReqs = updateCreatAclReqsList(handleDbRequests.getCreatedAclRequests(utilService.getUserName()));

        return updatedAclReqs;
    }

    private List<List<AclRequests>> updateCreatAclReqsList(List<AclRequests> topicsList){

        List<List<AclRequests>> newList = new ArrayList<>();
        List<AclRequests> innerList = new ArrayList<>();
        int modulusFactor = 3;
        int i=0;
        for(AclRequests topicInfo : topicsList){

            innerList.add(topicInfo);

            if(i%modulusFactor == (modulusFactor-1)) {
                newList.add(innerList);
                innerList = new ArrayList<>();
            }
            i++;
        }

        if(innerList.size()>0)
            newList.add(innerList);

        return newList;
    }

    public String deleteAclRequests(String req_no) {
        try {
            return "{\"result\":\""+handleDbRequests.deleteAclRequest(req_no)+"\"}";
        }catch(Exception e){
            return "{\"result\":\"failure "+e.toString()+"\"}";
        }
    }

    public String approveAclRequests(String req_no) throws KafkawizeException {

        if(!utilService.checkAuthorizedAdmin())
            return "{\"result\":\"Not Authorized\"}";

        AclRequests aclReq = handleDbRequests.selectAcl(req_no);
        if(aclReq.getReq_no() != null){
            ResponseEntity<String> response = clusterApiService.approveAclRequests(aclReq);

            String updateAclReqStatus ;

            try {
                if (response.getBody().equals("success"))
                    updateAclReqStatus = handleDbRequests.updateAclRequest(aclReq, utilService.getUserName());
                else
                    return "{\"result\":\"failure\"}";
            }catch(Exception e){
                return "{\"result\":\"failure "+e.toString()+"\"}";
            }
            return "{\"result\":\""+updateAclReqStatus+"\"}";
        }
        else
            return "{\"result\":\"Record not found !\"}";
    }

    public String declineAclRequests(String req_no) {

        if(!utilService.checkAuthorizedAdmin())
            return "{\"result\":\"Not Authorized\"}";

        AclRequests aclReq = handleDbRequests.selectAcl(req_no);
        String updateAclReqStatus ;

        if(aclReq.getReq_no() != null){
            try {
                 updateAclReqStatus = handleDbRequests.declineAclRequest(aclReq, utilService.getUserName());
                 return "{\"result\":\""+updateAclReqStatus+"\"}";
            }catch(Exception e){
                 return "{\"result\":\"failure "+e.toString()+"\"}";
            }
        }else
            return "{\"result\":\"Record not found !\"}";
    }

    private List<HashMap<String, String>> getAclListFromCluster(String bootstrapHost, boolean isSyncAcls, String topicNameSearch) throws KafkawizeException {
        List<HashMap<String, String>> aclList ;
        aclList = clusterApiService.getAcls(bootstrapHost);
        if(isSyncAcls)
            return updateConsumerGroups(groupAcls(aclList, topicNameSearch, true), aclList);
        else
            return updateConsumerGroups(groupAcls(aclList, topicNameSearch, false), aclList);
    }

    private List<HashMap<String, String>> updateConsumerGroups(List<HashMap<String, String>> groupedList, List<HashMap<String, String>> clusterAclList){
        List<HashMap<String, String>> updateList = new ArrayList<>(groupedList);

        for(HashMap<String, String> hMapGroupItem : groupedList){
            for(HashMap<String, String> hMapItem : clusterAclList){
                if(hMapGroupItem.get("operation").equals("READ") && hMapItem.get("operation").equals("READ") &&
                        hMapItem.get("resourceType").equals("GROUP")){
                    if(hMapItem.get("host").equals(hMapGroupItem.get("host")) && hMapItem.get("principle").equals(hMapGroupItem.get("principle"))){
                        HashMap<String, String> hashMap = new HashMap<>(hMapGroupItem);
                        hashMap.put("consumerGroup",hMapItem.get("resourceName"));
                        updateList.add(hashMap);
                        break;
                    }
                }
            }
        }
        return updateList;
    }

    private List<HashMap<String, String>> groupAcls(List<HashMap<String, String>> aclList, String topicNameSearch, boolean isSync){
        List<HashMap<String, String>> filteredList = aclList.stream()
                .filter(hItem->{
                    if(isSync){
                        if (hItem.get("resourceType").equals("TOPIC"))
                            return true;
                        else
                            return false;
                    }
                    else {
                        if (hItem.get("resourceName").equals(topicNameSearch))
                            return true;
                        else
                            return false;
                    }
                }).collect(Collectors.toList());

        return filteredList;
    }

    private List<Acl> getAclsFromSOT(String env, String topicNameSearch){
        List<Acl> aclsFromSOT = handleDbRequests.getSyncAcls(env);

        List<Acl> topicFilteredList = aclsFromSOT;
        // Filter topics on topic name for search
        if(topicNameSearch!=null && topicNameSearch.length()>0) {
            final String topicSearchFilter = topicNameSearch;
            topicFilteredList = aclsFromSOT.stream()
                    .filter(acl -> acl.getTopicname().contains(topicSearchFilter)
                    )
                    .collect(Collectors.toList());
        }

        aclsFromSOT = topicFilteredList;

        return aclsFromSOT;
    }

    public List<AclInfo> getAcls(String env, String pageNo, String topicNameSearch, boolean isSyncAcls) throws KafkawizeException {

        if(topicNameSearch != null)
            topicNameSearch = topicNameSearch.trim();

        if(isSyncAcls)
            if(!utilService.checkAuthorizedSU())
                return null;

        Env envSelected= handleDbRequests.selectEnvDetails(env);
        String bootstrapHost=envSelected.getHost()+":"+envSelected.getPort();

        List<HashMap<String,String>> aclList = getAclListFromCluster(bootstrapHost, isSyncAcls, topicNameSearch);
        List<Acl> aclsFromSOT = getAclsFromSOT(env, topicNameSearch);

        topicCounter = 0;

        if(!isSyncAcls){
            return applyFiltersAcls(env, aclList, aclsFromSOT, false)
                    .stream()
                    .collect(Collectors.groupingBy(w -> w.getTopicname()))
                    .get(topicNameSearch);
        }else{
            return getAclsList(pageNo,applyFiltersAcls(env, aclList, aclsFromSOT, true));
        }
    }

    private List<AclInfo> applyFiltersAcls(String env, List<HashMap<String,String>> aclList, List<Acl> aclsFromSOT, boolean isSyncAcls){

        List<AclInfo> aclListMap = new ArrayList<>() ;
        AclInfo mp ;
        List<String> teamList = new ArrayList<>();

        if(isSyncAcls) {
            handleDbRequests.selectAllTeamsOfUsers(utilService.getUserName())
                    .forEach(teamS -> teamList.add(teamS.getTeamname()));
        }

        for(HashMap<String,String> aclListItem : aclList)
        {
            mp = new AclInfo();
            mp.setEnvironment(env);

            if(isSyncAcls) {
                mp.setPossibleTeams(teamList);
                mp.setTeamname("");
            }

            String tmpPermType=aclListItem.get("operation");

            if(tmpPermType.equals("WRITE"))
                mp.setTopictype("Producer");
            else if(tmpPermType.equals("READ")){
                mp.setTopictype("Consumer");
                if(aclListItem.get("consumerGroup")!=null)
                    mp.setConsumergroup(aclListItem.get("consumerGroup"));
                else
                    continue;
            }

            if (aclListItem.get("resourceType").toLowerCase().equals("topic"))
                mp.setTopicname(aclListItem.get("resourceName"));

            mp.setAcl_ip(aclListItem.get("host"));
            mp.setAcl_ssl(aclListItem.get("principle"));

            for(Acl aclSotItem : aclsFromSOT)
            {
                String acl_ssl = aclSotItem.getAclssl();
                if(acl_ssl==null)
                    acl_ssl="User:*";

                String acl_host = aclSotItem.getAclip();
                if(acl_host==null)
                    acl_host="*";

                if( aclSotItem.getTopicname()!=null &&
                        aclListItem.get("resourceName").equals(aclSotItem.getTopicname()) &&
                        aclListItem.get("host").equals(acl_host) &&
                        aclListItem.get("principle").equals(acl_ssl) &&
                        aclSotItem.getTopictype().equals(mp.getTopictype()))
                {
                    mp.setTeamname(aclSotItem.getTeamname());
                    if(isSyncAcls)
                        mp.setReq_no(aclSotItem.getReq_no());
                    break;
                }
            }
            if(mp.getTeamname()==null)
                mp.setTeamname("Unknown");
            aclListMap.add(mp);

        }
        return aclListMap;
    }

    private List<AclInfo> getAclsList(String pageNo, List<AclInfo> aclListMap){
        List<AclInfo> aclListMapUpdated = new ArrayList<>();

        int totalRecs = aclListMap.size();
        int recsPerPage = 20;

        int totalPages = aclListMap.size()/recsPerPage + (aclListMap.size()%recsPerPage > 0 ? 1 : 0);

        int requestPageNo = Integer.parseInt(pageNo);
        int startVar = (requestPageNo-1) * recsPerPage;
        int lastVar = (requestPageNo) * (recsPerPage);
        for(int i=0;i<totalRecs;i++) {
            int counterInc = counterIncrement();
            if(i>=startVar && i<lastVar) {
                AclInfo mp = aclListMap.get(i);
                mp.setSequence(counterInc + "");

                mp.setTotalNoPages(totalPages + "");
                List<String> numList = new ArrayList<>();
                for (int k = 1; k <= totalPages; k++) {
                    numList.add("" + k);
                }
                mp.setAllPageNos(numList);
                aclListMapUpdated.add(mp);
            }
        }
        return aclListMapUpdated;
    }

    int topicCounter=0;
    private int counterIncrement()
    {
        topicCounter++;
        return topicCounter;
    }
}
