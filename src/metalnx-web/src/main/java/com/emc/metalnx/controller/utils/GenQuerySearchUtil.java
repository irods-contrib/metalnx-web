package com.emc.metalnx.controller.utils;

import static org.irods.jargon.core.query.QueryConditionOperators.EQUAL;
import static org.irods.jargon.core.query.QueryConditionOperators.GREATER_THAN;
import static org.irods.jargon.core.query.QueryConditionOperators.LESS_THAN;
import static org.irods.jargon.core.query.QueryConditionOperators.LIKE;
import static org.irods.jargon.core.query.QueryConditionOperators.NOT_EQUAL;
import static org.irods.jargon.core.query.QueryConditionOperators.NOT_LIKE;
import static org.irods.jargon.core.query.QueryConditionOperators.NUMERIC_EQUAL;
import static org.irods.jargon.core.query.QueryConditionOperators.NUMERIC_GREATER_THAN;
import static org.irods.jargon.core.query.QueryConditionOperators.NUMERIC_LESS_THAN;

import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.irods.jargon.core.connection.IRODSAccount;
import org.irods.jargon.core.exception.DataNotFoundException;
import org.irods.jargon.core.exception.InvalidArgumentException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.IRODSAccessObjectFactory;
import org.irods.jargon.core.pub.IRODSFileSystem;
import org.irods.jargon.core.pub.IRODSGenQueryExecutor;
import org.irods.jargon.core.query.GenQueryBuilderException;
import org.irods.jargon.core.query.GenQueryField.SelectFieldTypes;
import org.irods.jargon.core.query.IRODSGenQueryBuilder;
import org.irods.jargon.core.query.IRODSGenQueryFromBuilder;
import org.irods.jargon.core.query.IRODSQueryResultRow;
import org.irods.jargon.core.query.IRODSQueryResultSet;
import org.irods.jargon.core.query.JargonQueryException;
import org.irods.jargon.core.query.QueryConditionOperators;
import org.irods.jargon.core.query.RodsGenQueryEnum;

import com.emc.metalnx.core.domain.entity.DataGridCollectionAndDataObject;
import com.fasterxml.jackson.databind.JsonNode;

public class GenQuerySearchUtil
{
    private static final Map<String, RodsGenQueryEnum> mappedDataObjectColumns;
    private static final Map<String, RodsGenQueryEnum> mappedCollectionColumns;

    static
    {
        // @formatter:off
        mappedDataObjectColumns = new HashMap<>();
        mappedDataObjectColumns.put("DATA_OBJECT_NAME",         RodsGenQueryEnum.COL_DATA_NAME);
        mappedDataObjectColumns.put("COLLECTION",               RodsGenQueryEnum.COL_COLL_NAME);
        mappedDataObjectColumns.put("OWNER_NAME",               RodsGenQueryEnum.COL_D_OWNER_NAME);
        mappedDataObjectColumns.put("CREATION_DATE",            RodsGenQueryEnum.COL_D_CREATE_TIME);
        mappedDataObjectColumns.put("MODIFICATION_DATE",        RodsGenQueryEnum.COL_DATA_MODIFY_TIME);
        mappedDataObjectColumns.put("SIZE",                     RodsGenQueryEnum.COL_DATA_SIZE);
        mappedDataObjectColumns.put("CHECKSUM",                 RodsGenQueryEnum.COL_D_DATA_CHECKSUM);
        mappedDataObjectColumns.put("METADATA_ATTRIBUTE_NAME",  RodsGenQueryEnum.COL_META_DATA_ATTR_NAME);
        mappedDataObjectColumns.put("METADATA_ATTRIBUTE_VALUE", RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE);
        mappedDataObjectColumns.put("METADATA_ATTRIBUTE_UNITS", RodsGenQueryEnum.COL_META_DATA_ATTR_UNITS);

        mappedCollectionColumns = new HashMap<>();
        mappedCollectionColumns.put("COLLECTION",               RodsGenQueryEnum.COL_COLL_NAME);
        mappedCollectionColumns.put("OWNER_NAME",               RodsGenQueryEnum.COL_COLL_OWNER_NAME);
        mappedCollectionColumns.put("CREATION_DATE",            RodsGenQueryEnum.COL_COLL_CREATE_TIME);
        mappedCollectionColumns.put("MODIFICATION_DATE",        RodsGenQueryEnum.COL_COLL_MODIFY_TIME);
        mappedCollectionColumns.put("METADATA_ATTRIBUTE_NAME",  RodsGenQueryEnum.COL_META_COLL_ATTR_NAME);
        mappedCollectionColumns.put("METADATA_ATTRIBUTE_VALUE", RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE);
        mappedCollectionColumns.put("METADATA_ATTRIBUTE_UNITS", RodsGenQueryEnum.COL_META_COLL_ATTR_UNITS);
        // @formatter:on
    }

    public static final class SearchInput
    {
        public IRODSAccount account;

        public int offset;
        public int openSlots;
        public boolean caseInsensitive;

        public JsonNode attributes;
        public JsonNode operators;
        public JsonNode values;

        public SearchInput()
        {
            account = null;

            offset = 0;
            openSlots = 0;
            caseInsensitive = true;

            attributes = null;
            operators = null;
            values = null;
        }

        public SearchInput(SearchInput _other)
        {
            account = _other.account;

            offset = _other.offset;
            openSlots = _other.openSlots;
            caseInsensitive = _other.caseInsensitive;

            attributes = _other.attributes.deepCopy();
            operators = _other.operators.deepCopy();
            values = _other.values.deepCopy();
        }
    }

    public static final class SearchOutput
    {
        public List<DataGridCollectionAndDataObject> objects;
        public int matches;
    }
    
    public static SearchOutput search(SearchInput _input)
        throws GenQueryBuilderException, JargonException, JargonQueryException, ParseException
    {
        SearchInput searchInput = new SearchInput(_input);
        SearchOutput output = null;
        
        IRODSFileSystem fsys = IRODSFileSystem.instance();
        IRODSAccessObjectFactory factory = fsys.getIRODSAccessObjectFactory();
        IRODSGenQueryExecutor executor = factory.getIRODSGenQueryExecutor(_input.account);

        if (areColumnsSupportedByCollections(searchInput)) {
            output = findCollections(executor, searchInput);

            if (output.matches > 0) {
                searchInput.openSlots -= Math.min(output.matches, output.objects.size());
            }
            else {
            	// If we are beyond the collections (searchInput.offset > #collections),
            	// we must get the count so that we can calculate the appropriate offset
            	// for data objects.
                output.matches = countCollections(executor, searchInput);
            }
        }
        else {
            output = new SearchOutput();
            output.objects = new ArrayList<>();
            output.matches = 0;
        }
        
        // output.matches at this point is the number of collections so the offset
        // into the data objects is the overall (offset - #collections).
        searchInput.offset = Math.max(searchInput.offset - output.matches, 0);

        // Look for data objects matching the search criteria if there are empty
        // slots available.
        if (searchInput.openSlots > 0) {
            SearchOutput dataObjectSearchOutput = findDataObjects(executor, searchInput);
            output.objects.addAll(dataObjectSearchOutput.objects);
            output.matches += dataObjectSearchOutput.matches;
        }
        else {
            output.matches += countDataObjects(executor, searchInput);
        }
        
        factory.closeSessionAndEatExceptions();
        fsys.closeAndEatExceptions();
        
        return output;
    }

    public static SearchOutput findDataObjects(IRODSGenQueryExecutor executor, SearchInput _input)
        throws GenQueryBuilderException, JargonException, JargonQueryException, ParseException
    {
        return findObjectsImpl(executor, _input, false /* isCollection */);
    }

    public static SearchOutput findCollections(IRODSGenQueryExecutor executor, SearchInput _input)
        throws GenQueryBuilderException, JargonException, JargonQueryException, ParseException
    {
        return findObjectsImpl(executor, _input, true /* isCollection */);
    }

    public static int countDataObjects(IRODSGenQueryExecutor executor, SearchInput _input)
        throws GenQueryBuilderException, JargonException, JargonQueryException, ParseException
    {
        return countObjectsImpl(executor, _input, RodsGenQueryEnum.COL_DATA_NAME, false /* isCollection */);
    }

    public static int countCollections(IRODSGenQueryExecutor executor, SearchInput _input)
        throws GenQueryBuilderException, JargonException, JargonQueryException, ParseException
    {
        return countObjectsImpl(executor, _input, RodsGenQueryEnum.COL_COLL_NAME, true /* isCollection */);
    }

    private static SearchOutput findObjectsImpl(IRODSGenQueryExecutor executor, SearchInput _input, boolean _isCollection)
        throws GenQueryBuilderException, JargonException, JargonQueryException, ParseException
    {
        final boolean distinct = true;
        final boolean computeTotalRowCount = true;

        IRODSGenQueryBuilder gqlBuilder = new IRODSGenQueryBuilder(distinct, _input.caseInsensitive, computeTotalRowCount, null);

        if (_isCollection) {
            gqlBuilder
                .addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
                .addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_PARENT_NAME)
                .addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_MODIFY_TIME);
        }
        else {
            gqlBuilder
                .addSelectAsGenQueryValue(RodsGenQueryEnum.COL_DATA_NAME)
                .addSelectAsGenQueryValue(RodsGenQueryEnum.COL_COLL_NAME)
                .addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_DATA_SIZE, SelectFieldTypes.MAX)
                .addSelectAsAgregateGenQueryValue(RodsGenQueryEnum.COL_DATA_MODIFY_TIME, SelectFieldTypes.MAX);
        }

        addQueryConditions(gqlBuilder, _input, _isCollection);


        IRODSGenQueryFromBuilder gql = gqlBuilder.exportIRODSQueryFromBuilder(_input.openSlots);
        IRODSQueryResultSet resultSet = executor.executeIRODSQueryAndCloseResult(gql, _input.offset);

        List<DataGridCollectionAndDataObject> objects = new ArrayList<>();

        if (_isCollection) {
            for (IRODSQueryResultRow row : resultSet.getResults()) {
                objects.add(toDataGridCollection(row));
            }
        }
        else {
            for (IRODSQueryResultRow row : resultSet.getResults()) {
                objects.add(toDataGridDataObject(row));
            }
        }

        SearchOutput output = new SearchOutput();

        output.objects = objects;
        output.matches = resultSet.getTotalRecords();

        return output;
    }

    private static int countObjectsImpl(IRODSGenQueryExecutor executor, SearchInput _input, RodsGenQueryEnum _columnToCount, boolean _isCollection)
        throws GenQueryBuilderException, JargonException, JargonQueryException, ParseException
    {
        final boolean distinct = true;
        final boolean computeTotalRowCount = true;
        
        IRODSGenQueryBuilder gqlBuilder = new IRODSGenQueryBuilder(distinct, _input.caseInsensitive, computeTotalRowCount, null);

        if (_isCollection) {
            gqlBuilder.addSelectAsAgregateGenQueryValue(_columnToCount, SelectFieldTypes.COUNT);
        }
        else {
            // To avoid an incorrect count, we must return a list of data object information.
            // Using the GenQuery count() aggregate function for a data object with multiple
            // replicas will produce a value that counts replicas instead of data objects.
            gqlBuilder.addSelectAsGenQueryValue(_columnToCount);
        }

        addQueryConditions(gqlBuilder, _input, _isCollection);

        IRODSGenQueryFromBuilder gql = gqlBuilder.exportIRODSQueryFromBuilder(1 /* rows to return */);
        IRODSQueryResultSet resultSet = executor.executeIRODSQueryAndCloseResult(gql, 0 /* offset */);

        int count = 0;
        try {
            count = _isCollection
            	? resultSet.getFirstResult().getColumnAsIntOrZero(_columnToCount.getName())
            	: resultSet.getTotalRecords();
        } catch (DataNotFoundException e) {
        	// The count should always return a row but because of Jargon issue #495
        	// this will sometimes throw a DataNotFoundException if the previous getObjectImpl
        	// returned no data.
        	count = 0;
        }
        return count;
    }
    
    private static boolean areColumnsSupportedByCollections(SearchInput _input)
    {
        for (int i = 0; i < _input.attributes.size(); ++i) {
            if (!mappedCollectionColumns.containsKey(_input.attributes.get(i).textValue())) {
                return false;
            }
        }

        return true;
    }

    private static RodsGenQueryEnum toColumn(String _attribute, boolean _isCollection)
        throws InvalidArgumentException
    {
        if (_isCollection) {
            RodsGenQueryEnum col = mappedCollectionColumns.get(_attribute);

            if (null == col) {
                String fmt = "GenQuery column is not supported for collections: %s";
                throw new InvalidArgumentException(String.format(fmt, _attribute ));
            }

            return col;
        }

        RodsGenQueryEnum col = mappedDataObjectColumns.get(_attribute);

        if (null == col) {
            String fmt = "GenQuery column is not supported for data objects: %s";
            throw new InvalidArgumentException(String.format(fmt, _attribute ));
        }

        return col;
    }

    private static QueryConditionOperators toOperator(String _op, boolean _useNumericOp)
        throws InvalidArgumentException
    {
        // @formatter:off
        if      ("EQUAL".equals(_op))        { return _useNumericOp ? NUMERIC_EQUAL : EQUAL; }
        else if ("LIKE".equals(_op))         { return LIKE; }
        else if ("LESS_THAN".equals(_op))    { return _useNumericOp ? NUMERIC_LESS_THAN : LESS_THAN; }
        else if ("GREATER_THAN".equals(_op)) { return _useNumericOp ? NUMERIC_GREATER_THAN : GREATER_THAN; }
        else if ("NOT_EQUAL".equals(_op))    { return NOT_EQUAL; } // TODO No NUMERIC_NOT_EQUAL?
        else if ("NOT_LIKE".equals(_op))     { return NOT_LIKE; }
        // @formatter:on

        throw new InvalidArgumentException(String.format("GenQuery operator not supported: %s", _op));
    }

    private static boolean isWildcardConditionOperator(QueryConditionOperators _op)
    {
        return (LIKE == _op || NOT_LIKE == _op);
    }
    
    private static boolean isTimestamp(String _attribute)
    {
        return "CREATION_DATE".equals(_attribute) || "MODIFICATION_DATE".equals(_attribute);
    }
    
    private static long toSeconds(String _value) throws ParseException
    {
        return new SimpleDateFormat("MM/dd/yyyy hh:mm aa").parse(_value).getTime() / 1000;
    }

    private static void addQueryConditions(IRODSGenQueryBuilder _gqlBuilder, SearchInput _input, boolean _isCollection)
        throws InvalidArgumentException, ParseException
    {
        for (int i = 0; i < _input.attributes.size(); ++i) {
            String attr = _input.attributes.get(i).textValue();
            String val = _input.values.get(i).textValue();
            
            boolean useNumericOp = false;
            
            if (isTimestamp(attr)) {
                val = String.format("0%d", toSeconds(val));
            }
            else {
                useNumericOp = "SIZE".equals(attr);
            }

            QueryConditionOperators op = toOperator(_input.operators.get(i).textValue(), useNumericOp);

            if (isWildcardConditionOperator(op)) {
                val = String.format("%%%s%%", val.toUpperCase());
            }

            _gqlBuilder.addConditionAsGenQueryField(toColumn(attr, _isCollection), op, val);
        }
    }

    private static DataGridCollectionAndDataObject toDataGridDataObject(IRODSQueryResultRow _row)
        throws JargonException
    {
        String dataName = _row.getColumn("DATA_NAME");
        String collName = _row.getColumn("COLL_NAME");
        String dataSize = _row.getColumn("DATA_SIZE");

        DataGridCollectionAndDataObject object = new DataGridCollectionAndDataObject();

        object.setCollection(false);
        object.setPath(String.format("%s/%s", collName, dataName));
        object.setName(dataName);
        object.setParentPath(collName);
        object.setModifiedAt(new Date(Long.parseLong(_row.getColumn("DATA_MODIFY_TIME")) * 1000));
        object.setSize(Long.parseLong(dataSize));
        object.setDisplaySize(dataSize);
        object.setVisibleToCurrentUser(true);
        object.setOwner("");

        return object;
    }

    private static DataGridCollectionAndDataObject toDataGridCollection(IRODSQueryResultRow _row)
        throws JargonException
    {
        String collName = _row.getColumn("COLL_NAME");

        DataGridCollectionAndDataObject object = new DataGridCollectionAndDataObject();

        object.setCollection(true);
        object.setPath(collName);
        object.setParentPath(_row.getColumn("COLL_PARENT_NAME"));
        object.setName("/".equals(collName) ? collName : Paths.get(collName).getFileName().toString());
        object.setModifiedAt(new Date(Long.parseLong(_row.getColumn("COLL_MODIFY_TIME")) * 1000));
        object.setSize(0);
        object.setDisplaySize("0");
        object.setVisibleToCurrentUser(true);
        object.setOwner("");

        return object;
    }
}
