/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.modelattribute.collection;

/**
 * Class that represents a form that contains information about a collection or
 * data object that is about to change. For collections it contains the name and
 * the inherit option and for data object it only contains name.
 */
public class CollectionOrDataObjectForm {

    private String collectionName;
    private String path;
    private String parentPath;
    private boolean inheritOption;
    private boolean isCollection;

    /**
     * @return the collectionName
     */
    public String getCollectionName() {
        return collectionName;
    }

    /**
     * @param collectionName
     *            the collectionName to set
     */
    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName.trim();
    }

    /**
     * @return the inheritOption
     */
    public boolean getInheritOption() {
        return inheritOption;
    }

    /**
     * @param inheritOption
     *            the inheritOption to set
     */
    public void setInheritOption(boolean inheritOption) {
        this.inheritOption = inheritOption;
    }

    /**
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path
     *            the path to set
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return the parentPath
     */
    public String getParentPath() {
        return parentPath;
    }

    /**
     * @param parentPath
     *            the parentPath to set
     */
    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    /**
     * @return the isCollection
     */
    public boolean isCollection() {
        return isCollection;
    }

    /**
     * @param isCollection
     *            the isCollection to set
     */
    public void setCollection(boolean isCollection) {
        this.isCollection = isCollection;
    }
}
