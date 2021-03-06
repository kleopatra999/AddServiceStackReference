package net.servicestack.tests;

import static org.junit.Assert.*;
import static net.servicestack.tests.techstacks.*;

import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.servicestack.client.ConnectionFilter;
import net.servicestack.client.ExceptionFilter;
import net.servicestack.client.HttpMethods;
import net.servicestack.client.JsonServiceClient;
import net.servicestack.client.Log;
import net.servicestack.client.MimeTypes;
import net.servicestack.client.ResponseStatus;
import net.servicestack.client.TimeSpan;
import net.servicestack.client.Utils;
import net.servicestack.client.WebServiceException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;


public class TechStacksTests {

    JsonServiceClient client = new JsonServiceClient("http://techstacks.io");

//	@BeforeClass 
//    public static void setUpClass() {      
//		JsonServiceClient.GlobalRequestFilter = new ConnectionFilter() {
//		    @Override public void exec(HttpURLConnection conn) {
//		        String verb = conn.getRequestMethod();
//		        if (verb == HttpMethods.Post || verb == HttpMethods.Put) {
//		            conn.setDoOutput(true);
//		        }
//		    }
//		};
//	}

    @Test
    public void test_Can_GET_TechStacks_Overview(){
        OverviewResponse response = client.get(new Overview());

        assertOverviewResponse(response);
    }

    @Test
    public void test_Can_GET_TechStacks_AppOverview(){
        AppOverviewResponse r = client.get(new AppOverview());
        assertNotNull(r);
        assertTrue(r.getTopTechnologies().size() > 0);
        assertTrue(r.getAllTiers().size() > 0);
    }

    @Test
    public void test_Can_GET_TechStacks_Overview_with_relative_url() {
        OverviewResponse response = client.get("/overview", OverviewResponse.class);
        assertOverviewResponse(response);
    }

    @Test
    public void test_Can_GET_TechStacks_Overview_with_absolute_url() {
        OverviewResponse response = client.get("http://techstacks.io/overview", OverviewResponse.class);
        assertOverviewResponse(response);
    }

    @Test
    public void test_Can_GET_GetTechnology_with_params() {
        GetTechnology requestDto = new GetTechnology()
            .setSlug("servicestack");

        GetTechnologyResponse response = client.get(requestDto);
        assertGetTechnologyResponse(response);
    }

    @Test
    public void test_Can_GET_GetTechnology_with_url() {
        GetTechnologyResponse response = client.get("/technology/servicestack", GetTechnologyResponse.class);
        assertGetTechnologyResponse(response);
    }

    @Test
    public void test_Can_call_FindTechnologies_AutoQuery_Service() {
        FindTechnologies request = new FindTechnologies()
            .setName("ServiceStack");

        QueryResponse<Technology> response = client.get(request);

        assertEquals(1, response.getResults().size());
    }

    @Test
    public void test_Can_call_FindTechnologies_AutoQuery_Implicit_Service() {
        FindTechnologies request = (FindTechnologies) new FindTechnologies()
            .setTake(5);

        QueryResponse<Technology> response = client.get(request,
            Utils.createMap("DescriptionContains","framework"));

        assertEquals(5, response.getResults().size());
    }

    @Test
    public void test_Can_serialize_Empty_Option() {
        Option dto = new Option();

        String json = client.toJson(dto);

        assertEquals("{}", json);
    }

    @Test
    public void test_Can_deserialize_Empty_Option() {
        String json = "{\"name\":null,\"title\":null,\"value\":null}";

        Option dto = (Option)client.fromJson(json, Option.class);

        assertNull(dto.getName());
        assertNull(dto.getTitle());
        assertNull(dto.getValue());
    }

    @Test
    public void test_Can_serialize_Full_Option() {
        Option dto = new Option()
            .setName("name")
            .setTitle("title")
            .setValue(TechnologyTier.ProgrammingLanguage);

        String json = client.toJson(dto);

        assertEquals("{\"name\":\"name\",\"title\":\"title\",\"value\":\"ProgrammingLanguage\"}", json);
    }

    @Test
    public void test_Can_deserialize_Full_Option() {
        String json = "{\"name\":\"name\",\"title\":\"title\",\"value\":\"ProgrammingLanguage\"}";

        Option dto = (Option)client.fromJson(json, Option.class);

        assertEquals("name", dto.getName());
        assertEquals("title", dto.getTitle());
        assertEquals(TechnologyTier.ProgrammingLanguage, dto.getValue());
    }
    
    @Test
    public void test_Can_handle_Unauthorized_Response()
    {
    	try {
    	    //Auth required, should throw a WebServiceException
    	    client.post(new Overview());
    	    fail("Should throw WebServiceException");
    	} catch (WebServiceException ex) {
    		assertEquals("Unauthorized", ex.getStatusDescription());
    	}
    }
    
    @Test
    public void test_Can_deserialize_Overview() throws IOException {

        InputStream is = TechStacksTests.class.getResourceAsStream("/overview.json");
        String json = Utils.readToEnd(is, "UTF-8");

        OverviewResponse dto = (OverviewResponse)client.fromJson(json, OverviewResponse.class);

        assertEquals(6, dto.getTopUsers().size());
        assertEquals("demisbellot", dto.getTopUsers().get(0).getUserName());
        assertEquals("http:\\/\\/pbs.twimg.com\\/profile_images\\/1765666853\\/image1326949938_normal.png", dto.getTopUsers().get(0).getAvatarUrl());
        assertEquals(61, (int)dto.getTopUsers().get(0).getStacksCount());


        assertEquals(20 ,dto.getTopTechnologies().size());
        TechnologyInfo topTech = dto.getTopTechnologies().get(0);
        assertEquals(TechnologyTier.Data, topTech.getTier());
        assertEquals("redis", topTech.getSlug());
        assertEquals("Redis", topTech.getName());
        assertEquals("https://raw.githubusercontent.com/ServiceStack/Assets/master/img/livedemos/techstacks/redis-logo.png", topTech.getLogoUrl());
        assertEquals(35, (int)topTech.getStacksCount());


        ArrayList<TechStackDetails> latestStacks = dto.getLatestTechStacks();
        assertEquals(20, latestStacks.size());

        TechStackDetails techstacks = latestStacks.get(0);
        assertEquals(1, (long)techstacks.getId());
        assertEquals("TechStacks Website", techstacks.getName());
        assertEquals("ServiceStack", techstacks.getVendorName());
        assertTrue(techstacks.Description.startsWith("This Website! "));
        assertEquals("http://techstacks.io", techstacks.getAppUrl());
        assertEquals("https://raw.githubusercontent.com/ServiceStack/Assets/master/img/livedemos/techstacks/screenshots/techstacks.png", techstacks.getScreenshotUrl());
        assertEquals(Utils.parseDate("2015-01-01T17:33:58.9892560"), techstacks.getCreated());
        assertEquals("layoric", techstacks.getCreatedBy());
        assertEquals(Utils.parseDate("2015-01-12T23:34:12.4516410"), techstacks.getLastModified());
        assertEquals("layoric", techstacks.getLastModifiedBy());
        assertTrue(techstacks.getIsLocked());
        assertEquals("2", techstacks.getOwnerId());
        assertEquals("techstacks-website", techstacks.getSlug());
        assertEquals(Utils.parseDate("2015-01-12T23:34:12.4516410"), techstacks.getLastStatusUpdate());

        ArrayList<TechnologyInStack> techstackChoices = techstacks.getTechnologyChoices();
        assertEquals(10, techstackChoices.size());
        TechnologyInStack techChoice = techstackChoices.get(0);
        assertEquals(1, (long)techChoice.getTechnologyId());
        assertEquals(1, (long)techChoice.getTechnologyStackId());
        assertEquals(2, (long)techChoice.getId());
        assertEquals("ServiceStack", techChoice.getName());
        assertEquals("Service Stack", techChoice.getVendorName());
        assertEquals("https://servicestack.net", techChoice.getVendorUrl());
        assertEquals("https://servicestack.net", techChoice.getProductUrl());
        assertEquals("https://raw.githubusercontent.com/ServiceStack/Assets/master/img/livedemos/techstacks/servicestack-logo.png", techChoice.getLogoUrl());
        assertEquals(Utils.parseDate("2014-12-28T08:49:20.9542550"), techChoice.getCreated());
        assertEquals("demisbellot", techChoice.getCreatedBy());
        assertEquals(Utils.parseDate("2014-12-28T08:49:20.9542550"), techChoice.getLastModified());
        assertEquals("demisbellot", techChoice.getLastModifiedBy());
        assertEquals("1", techChoice.getOwnerId());
        assertEquals("servicestack", techChoice.getSlug());
        assertTrue(techChoice.isLogoApproved());
        assertFalse(techChoice.getIsLocked());
        assertEquals(TechnologyTier.Server, techChoice.getTier());


        assertEquals(9, dto.getTopTechnologiesByTier().size());
        ArrayList<TechnologyInfo> langs = dto.getTopTechnologiesByTier().get(TechnologyTier.ProgrammingLanguage);
        assertEquals(3, langs.size());
        TechnologyInfo lang = langs.get(0);
        assertEquals(TechnologyTier.ProgrammingLanguage, lang.getTier());
        assertEquals("python", lang.getSlug());
        assertEquals("Python", lang.getName());
        assertEquals("https://raw.githubusercontent.com/ServiceStack/Assets/master/img/livedemos/techstacks/python-logo.png", lang.getLogoUrl());
        assertEquals(25, (int)lang.getStacksCount());

//        let toJson = dto.toJson()
//        println(toJson)
    }
    
    /* TEST HELPERS */

    public static void assertOverviewResponse(OverviewResponse r) {
        assertNotNull(r);
        assertTrue(r.getTopUsers().size() > 0);
        assertTrue(r.getTopTechnologies().size() > 0);
        assertTrue(r.getLatestTechStacks().size() > 0);
        assertTrue(r.getLatestTechStacks().get(0).getTechnologyChoices().size() > 0);
        assertTrue(r.getTopTechnologiesByTier().size() > 0);
    }

    public static void assertGetTechnologyResponse(GetTechnologyResponse r) {
        assertNotNull(r);
        assertEquals("ServiceStack", r.getTechnology().getName());
        assertTrue(r.getTechnologyStacks().size() > 0);
    }
}