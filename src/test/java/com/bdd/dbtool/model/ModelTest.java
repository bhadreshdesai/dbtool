package com.bdd.dbtool.model;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import javax.xml.bind.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.*;

public class ModelTest {
    @Test
    public void testHashCode() {
        Database database = getTestDatabase();
        int oldHash = database.hashCode();
        database.getTables().get(0).setName("fail");
        int newHash1 = database.hashCode();
        assertNotEquals(oldHash, newHash1);
        database.getTables().get(0).getColumns().get(0).setName("fail");
        int newHash2 = database.hashCode();
        assertNotEquals(oldHash, newHash2);
        assertNotEquals(newHash1, newHash2);
    }

    @Test
    public void testObjToXml() throws JAXBException, IOException {
        JAXBContext context = JAXBContext.newInstance(Database.class);
        Marshaller mar = context.createMarshaller();
        mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

        Database testDatabase = getTestDatabase();
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<Database> je =  objectFactory.createDatabase(testDatabase);

        //mar.marshal(je, new File("./build/database.xml"));
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        mar.marshal(je, stream);
        String actualDatabaseXML = new String(stream.toByteArray());
        // form some reason jaxb formatter is always using "\n" for line endings
        // replace it with system line separator.
        // git will checkout the expected file with system line separator
        actualDatabaseXML = actualDatabaseXML.replaceAll("\n", System.getProperty("line.separator"));

        String expectedDatabaseXML = getTestDatabaseXML();
        assertEquals(expectedDatabaseXML, actualDatabaseXML);
    }

    @Test
    public void testToString() {
        String expectedString = "Database(tables=[Table(name=table0, remarks=table remarks 0, columns=[Column(name=t0c0, dataType=0, remarks=Table 0 column 0), Column(name=t0c1, dataType=1, remarks=Table 0 column 1), Column(name=t0c2, dataType=2, remarks=Table 0 column 2), Column(name=t0c3, dataType=3, remarks=Table 0 column 3), Column(name=t0c4, dataType=4, remarks=Table 0 column 4)]), Table(name=table1, remarks=table remarks 1, columns=[Column(name=t1c0, dataType=0, remarks=Table 1 column 0), Column(name=t1c1, dataType=1, remarks=Table 1 column 1), Column(name=t1c2, dataType=2, remarks=Table 1 column 2), Column(name=t1c3, dataType=3, remarks=Table 1 column 3), Column(name=t1c4, dataType=4, remarks=Table 1 column 4)]), Table(name=table2, remarks=table remarks 2, columns=[Column(name=t2c0, dataType=0, remarks=Table 2 column 0), Column(name=t2c1, dataType=1, remarks=Table 2 column 1), Column(name=t2c2, dataType=2, remarks=Table 2 column 2), Column(name=t2c3, dataType=3, remarks=Table 2 column 3), Column(name=t2c4, dataType=4, remarks=Table 2 column 4)])])";
        String actualString = getTestDatabase().toString();
        assertEquals(expectedString, actualString);
    }

    @Test
    public void testXmlToObj() throws JAXBException {
        Database expectedDatabase = getTestDatabase();
        Database actualDatabase = unmarshallDatabase();
        assertEquals(expectedDatabase, actualDatabase);
    }

    @Test
    public void testXmlToObjTableFail() throws JAXBException {
        Database unexpectedDatabase = getTestDatabase();
        Database actualDatabase = unmarshallDatabase();
        unexpectedDatabase.getTables().get(0).setName("fail");
        assertNotEquals(unexpectedDatabase, actualDatabase);
    }

    @Test
    public void testXmlToObjColumnFail() throws JAXBException {
        Database unexpectedDatabase = getTestDatabase();
        Database actualDatabase = unmarshallDatabase();
        unexpectedDatabase.getTables().get(0).getColumns().get(0).setName("fail");
        assertNotEquals(unexpectedDatabase, actualDatabase);
    }

    private Database unmarshallDatabase() throws JAXBException {
        // Get test database xml file
        File testDatabaseXMLFile = getTestDatabaseXMLFile();

        // Unmarshal database from the test database xml file
        JAXBContext jc = JAXBContext.newInstance("com.bdd.dbtool.model");
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        return (Database) JAXBIntrospector.getValue(unmarshaller.unmarshal(testDatabaseXMLFile));
    }

    private Database getTestDatabase() {
        ObjectFactory objectFactory = new ObjectFactory();
        List<Table> tables = new ArrayList<>();
        for (int t = 0; t < 3; t++) {
            String tableName = "table" + t;
            List<Column> columns = new ArrayList<>();
            for (int c = 0; c < 5; c++) {
                Column column = objectFactory.createColumn();
                column.setName("t"+t+"c"+c);
                column.setDataType(c);
                column.setRemarks("Table " + t + " column " + c);
                columns.add(column);
            }
            Table table = objectFactory.createTable();
            table.setName(tableName);
            table.setRemarks("table remarks " + t);
            table.setColumns(columns);
            tables.add(table);
        }
        Database database = objectFactory.createDatabase();
        database.setTables(tables);
        return database;
    }

    private String getTestDatabaseXML() throws IOException {
        return FileUtils.readFileToString(getTestDatabaseXMLFile(), StandardCharsets.UTF_8);
    }

    private File getTestDatabaseXMLFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        return new File(Objects.requireNonNull(classLoader.getResource("database.xml")).getFile());
    }
}
