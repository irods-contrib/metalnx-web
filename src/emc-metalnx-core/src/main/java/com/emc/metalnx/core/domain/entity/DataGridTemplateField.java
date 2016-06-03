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
package com.emc.metalnx.core.domain.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import com.emc.metalnx.core.domain.exceptions.DataGridTemplateAttrException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateUnitException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateValueException;

@Entity
@Audited
@Table(name = "template_fields")
public class DataGridTemplateField implements Serializable, Comparable<DataGridTemplateField> {

    private static final long serialVersionUID = 1L;

    private final int MAX_ATTR_LENGTH = 100;
    private final int MAX_VAL_LENGTH = 100;
    private final int MAX_UNT_LENGTH = 100;

    @Id
    @NotAudited
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "template_field_id", unique = true, nullable = false)
    private Long id;

    @Column(name = "attribute", length = MAX_ATTR_LENGTH)
    private String attribute;

    @Column(name = "attribute_value", length = MAX_VAL_LENGTH)
    private String attributeValue;

    @Column(name = "attribute_unit", length = MAX_UNT_LENGTH)
    private String attributeUnit;

    @Column(name = "start_range")
    private float startRange;

    @Column(name = "end_range")
    private float endRange;

    @Column(name = "field_order")
    private int order;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "template_id", nullable = false, updatable = true)
    private DataGridTemplate template;

    public DataGridTemplateField() {

    }

    public DataGridTemplateField(String attribute, String value, String unit, DataGridTemplate template) throws DataGridTemplateAttrException,
            DataGridTemplateValueException, DataGridTemplateUnitException {
        if (attribute == null || attribute.length() > MAX_ATTR_LENGTH) {
            throw new DataGridTemplateAttrException("Template attribute is null or exceed " + MAX_ATTR_LENGTH + " characters.");
        }

        if (value == null || value.length() > MAX_VAL_LENGTH) {
            throw new DataGridTemplateValueException("Template attribute is null or exceed " + MAX_ATTR_LENGTH + " characters.");
        }

        if (unit == null || unit.length() > MAX_UNT_LENGTH) {
            throw new DataGridTemplateUnitException("Template attribute is null or exceed " + MAX_ATTR_LENGTH + " characters.");
        }

        this.attribute = attribute;
        attributeValue = value;
        attributeUnit = unit;
        this.template = template;
    }

    public DataGridTemplateField(String attribute, String value, String unit) throws DataGridTemplateAttrException, DataGridTemplateValueException,
            DataGridTemplateUnitException {
        this(attribute, value, unit, null);
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return the attribute
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * @param attribute
     *            the attribute to set
     * @throws DataGridTemplateAttrException
     */
    public void setAttribute(String attribute) throws DataGridTemplateAttrException {
        if (attribute == null || attribute.length() > MAX_ATTR_LENGTH) {
            throw new DataGridTemplateAttrException("Template attribute is null or exceed " + MAX_ATTR_LENGTH + " characters.");
        }

        this.attribute = attribute;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return attributeValue;
    }

    /**
     * @param value
     *            the value to set
     * @throws DataGridTemplateValueException
     */
    public void setValue(String value) throws DataGridTemplateValueException {
        if (value == null || value.length() > MAX_VAL_LENGTH) {
            throw new DataGridTemplateValueException("Template attribute is null or exceed " + MAX_ATTR_LENGTH + " characters.");
        }

        attributeValue = value;
    }

    /**
     * @return the unit
     */
    public String getUnit() {
        return attributeUnit;
    }

    /**
     * @param unit
     *            the unit to set
     * @throws DataGridTemplateUnitException
     */
    public void setUnit(String unit) throws DataGridTemplateUnitException {
        if (unit == null || unit.length() > MAX_UNT_LENGTH) {
            throw new DataGridTemplateUnitException("Template attribute is null or exceed " + MAX_ATTR_LENGTH + " characters.");
        }

        attributeUnit = unit;
    }

    /**
     * @return the template
     */
    public DataGridTemplate getTemplate() {
        return template;
    }

    /**
     * @param template
     *            the template to set
     */
    public void setTemplate(DataGridTemplate template) {
        this.template = template;
    }

    /**
     * @return the startRange
     */
    public float getStartRange() {
        return startRange;
    }

    /**
     * @param startRange
     *            the startRange to set
     */
    public void setStartRange(float startRange) {
        this.startRange = startRange;
    }

    /**
     * @return the endRange
     */
    public float getEndRange() {
        return endRange;
    }

    /**
     * @param endRange
     *            the endRange to set
     */
    public void setEndRange(float endRange) {
        this.endRange = endRange;
    }

    /**
     * @return the order
     */
    public int getOrder() {
        return order;
    }

    /**
     * @param order
     *            the order to set
     */
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Id: ");
        sb.append(id);
        sb.append("\nAttribute: ");
        sb.append(getAttribute());
        sb.append("\nValue: ");
        sb.append(getValue());
        sb.append("\nUnit: ");
        sb.append(getUnit());
        sb.append("\nOrder: ");
        sb.append(getOrder());
        sb.append("\nStart Range: ");
        sb.append(getStartRange());
        sb.append("\nEnd Range: ");
        sb.append(getEndRange());
        sb.append("\nTemplate: ");
        sb.append(getTemplate().getTemplateName());
        return sb.toString();
    }

    @Override
    public int compareTo(DataGridTemplateField dgmf) {
        if (attribute != null) {
            return attribute.compareTo(dgmf.getAttribute());
        }
        else if (attributeValue != null) {
            return attributeValue.compareTo(dgmf.getValue());
        }
        else if (attributeUnit != null) {
            return attributeUnit.compareTo(dgmf.getUnit());
        }

        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj instanceof DataGridTemplateField) {
            DataGridTemplateField dgmf = (DataGridTemplateField) obj;

            if (dgmf.getAttribute() == null || dgmf.getValue() == null) {
                return false;
            }

            boolean areAttributesEqual = getAttribute().equals(dgmf.getAttribute());
            boolean areValuesEqual = getValue().equals(dgmf.getValue());

            if (areAttributesEqual && areValuesEqual) {
                return true;
            }
        }

        return false;
    }

    @Override
    public int hashCode() {
        return (getAttribute() + getValue()).hashCode();
    }
}
