/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.core.domain.entity;

/**
 * Class that represents a ticket.
 */
public class DataGridTicket {
    private String ticketString, path, owner;
    private TicketType type;
    private boolean isCollection;

    public enum TicketType {
        READ, WRITE, UNKNOWN;
    }

    public void setTicketString(String ticketString) {
        this.ticketString = ticketString;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setType(TicketType type) {
        this.type = type;
    }

    /**
     * Tells whether or not the ticket is for a collection.
     * @param isTicketForCollection True if the path associated to the ticket is a collection. False, otherwise.
     */
    public void setIsCollection(boolean isTicketForCollection) {
        isCollection = isTicketForCollection;
    }

    public String getTicketString() {
        return ticketString;
    }

    public String getPath() {
        return path;
    }

    public String getOwner() {
        return owner;
    }

    public TicketType getType() {
        return type;
    }

    public boolean isCollection() {
        return isCollection;
    }

    @Override
    public String toString() {
        return "DataGridTicket{" +
                "ticketString='" + ticketString + '\'' +
                ", path='" + path + '\'' +
                ", owner='" + owner + '\'' +
                ", type=" + type +
                ", isCollection=" + isCollection +
                '}';
    }
}
