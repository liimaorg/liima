/*
 * AMW - Automated Middleware allows you to manage the configurations of
 * your Java EE applications on an unlimited number of different environments
 * with various versions, including the automated deployment of those apps.
 * Copyright (C) 2013-2016 by Puzzle ITC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package ch.mobi.itc.mobiliar.rest.dtos;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import ch.puzzle.itc.mobiliar.business.resourcerelation.entity.ConsumedResourceRelationEntity;

/**
 * DTO für JavaBatch Monitor: ein Job im Job-Inventar
 * @author U110565
 *
 */
@XmlRootElement(name = "batchRelations")
@XmlAccessorType(XmlAccessType.FIELD)
public class BatchResourceRelationDTO {

    private String batchApp; //z.B. ch_mobi_fofa_fofa_selection
    private String appName; //short appName 'fofa_batch', in AMW used as 'appServer'
    private String batchAppRelease;
    private Date batchAppReleaseDate;
    private String batchJobName; //AMW Name always 'standardJob'
    private String jobName;
    private String batchJobIdentifier; //AMW Name with identifying Number, e.g. standardJob_2
    private String batchJobRelease;
    private Date batchJobReleaseDate;
    private String batchServer; //z.B. sd06432.umobi.mobicorp.test
    
    //batch app consumes the following resources:
    private boolean db2 = false;
    private boolean oracle = false;
    private boolean ws = false;
    private boolean file = false;
    private List<String> wsList = new ArrayList<>(); //store List of consumed ws for filtering

    BatchResourceRelationDTO(){}

    public BatchResourceRelationDTO(ConsumedResourceRelationEntity relation){
        batchApp = relation.getMasterResourceName(); 
        batchAppRelease = relation.getMasterRelease();
        batchAppReleaseDate = relation.getMasterResource().getRelease().getInstallationInProductionAt();
        batchJobName = relation.getSlaveResource().getName();
        batchJobRelease = relation.getSlaveResource().getRelease().getName();
        batchJobReleaseDate = relation.getSlaveResource().getRelease().getInstallationInProductionAt();
        batchJobIdentifier = relation.buildIdentifer();
    }

    public void setBatchServer(String batchServer) {
        this.batchServer = batchServer;
    }

    public void setDb2(boolean db2) {
        this.db2 = db2;
    }

    public void setOracle(boolean oracle) {
        this.oracle = oracle;
    }

    public void setWs(boolean ws) {
        this.ws = ws;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public String getBatchApp() {
        return batchApp;
    }

    public void setBatchApp(String batchApp) {
        this.batchApp = batchApp;
    }

    public String getBatchAppRelease() {
        return batchAppRelease;
    }

    public void setBatchAppRelease(String batchAppRelease) {
        this.batchAppRelease = batchAppRelease;
    }

    public String getBatchJobName() {
        return batchJobName;
    }

    public void setBatchJobName(String batchJobName) {
        this.batchJobName = batchJobName;
    }

    public String getBatchJobRelease() {
        return batchJobRelease;
    }

    public void setBatchJobRelease(String batchJobRelease) {
        this.batchJobRelease = batchJobRelease;
    }

    public String getBatchJobIdentifier() {
        return batchJobIdentifier;
    }

    public void setBatchJobIdentifier(String batchJobIdentifier) {
        this.batchJobIdentifier = batchJobIdentifier;
    }

    public String getBatchServer() {
        return batchServer;
    }

    public boolean getDb2() {
        return db2;
    }

    public boolean getOracle() {
        return oracle;
    }

    public boolean getWs() {
        return ws;
    }

    public boolean getFile() {
        return file;
    }
    
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Date getBatchAppReleaseDate() {
        return batchAppReleaseDate;
    }

    public void setBatchAppReleaseDate(Date batchAppReleaseDate) {
        this.batchAppReleaseDate = batchAppReleaseDate;
    }

    public Date getBatchJobReleaseDate() {
        return batchJobReleaseDate;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public void setBatchJobReleaseDate(Date batchJobReleaseDate) {
        this.batchJobReleaseDate = batchJobReleaseDate;
    }

    public List<String> getWsList() {
        return wsList ;  
    }
    
}
