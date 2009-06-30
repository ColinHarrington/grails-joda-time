package com.energizedwork.grails.plugins.jodatime

import com.energizedwork.grails.plugins.jodatime.test.Person
import functionaltestplugin.FunctionalTestCase
import javax.servlet.http.HttpServletResponse
import org.joda.time.LocalDate
import org.joda.time.LocalTime
import org.joda.time.format.DateTimeFormat
import org.w3c.dom.Element
import static javax.servlet.http.HttpServletResponse.SC_OK
import org.joda.time.format.ISODateTimeFormat

class ScaffoldingTests extends FunctionalTestCase {

	def rob

	void setUp() {
		super.setUp()

		def port = System.properties."server.port" ?: 8080
		baseURL = "http://localhost:${port}/jodaTimeTest"

		rob = Person.build(name: "Rob", birthday: new LocalDate(1971, 11, 29), alarmClock: new LocalTime(6, 0))
	}

	void tearDown() {
		super.tearDown()
		Person.list()*.delete(flush: true)
	}

	void testEmptyListView() {
		get "/person"
		assertStatus SC_OK
		assertTitle "Person List"
	}

	void testCreatePerson() {
		get "/person/create"
		assertStatus SC_OK
		assertTitle "Create Person"

		form() {
			name = "Alex"
			alarmClock_hour = "07"
			alarmClock_minute = "00"
			birthday_day = "2"
			birthday_month = "10"
			birthday_year = "2008"
			click "Create"
		}

		assertContentContains "Person ${rob.id + 1} created"

		def alex = Person.findByName("Alex")
		assertEquals(new LocalDate(2008, 10, 2), alex.birthday)
		assertEquals(new LocalTime(7, 0), alex.alarmClock)
	}

	void testShowPerson() {
		get("/person/show/$rob.id"){
			headers["Accept-Language"] = "en_GB"
		}
		assertStatus SC_OK
		assertTitle "Show Person"

		assertTextByXPath("Rob", "//tr[td[1]/text() = 'Name:']/td[@class='value']")
		assertTextByXPath("06:00", "//tr[td[1]/text() = 'Alarm Clock:']/td[@class='value']")
		assertTextByXPath("29/11/71", "//tr[td[1]/text() = 'Birthday:']/td[@class='value']")

		String createdDate = byXPath("//tr[td[1]/text() = 'Date Created:']/td[@class='value']")?.textContent
		try {
//			DateTimeFormat.forStyle("SS").withLocale(Locale.default).parseDateTime(createdDate)
			ISODateTimeFormat.dateTime().parseDateTime(createdDate)
		} catch(IllegalArgumentException e) {
			fail "Could not parse '$createdDate' as DateTime"
		}
	}

	void testEditPerson() {
		get "/person/edit/$rob.id"
		assertStatus SC_OK
		assertTitle "Edit Person"

		form() {
			assertEquals("Rob", name)
			assertEquals(["06"], alarmClock_hour)
			assertEquals(["00"], alarmClock_minute)
			assertEquals(["29"], birthday_day)
			assertEquals(["11"], birthday_month)
			assertEquals(["1971"], birthday_year)
		}
	}

	void testListViewIsSortable() {
		Person.build(name: "Ilse", birthday: new LocalDate(1972, 7, 6), alarmClock: new LocalTime(7, 15))
		Person.build(name: "Alex", birthday: new LocalDate(2008, 10, 2), alarmClock: new LocalTime(7, 0))

		get "/person/list"
		assertStatus SC_OK
		assertTitle "Person List"

		// sort by local time
		click "Alarm Clock"
		assertEquals("Rob", byXPath("//tbody/tr[1]/td[2]").textContent)
		assertEquals("Alex", byXPath("//tbody/tr[2]/td[2]").textContent)
		assertEquals("Ilse", byXPath("//tbody/tr[3]/td[2]").textContent)

		// sort descending
		click "Alarm Clock"
		assertEquals("Ilse", byXPath("//tbody/tr[1]/td[2]").textContent)
		assertEquals("Alex", byXPath("//tbody/tr[2]/td[2]").textContent)
		assertEquals("Rob", byXPath("//tbody/tr[3]/td[2]").textContent)

		// sort by local date
		click "Birthday"
		assertEquals("Rob", byXPath("//tbody/tr[1]/td[2]").textContent)
		assertEquals("Ilse", byXPath("//tbody/tr[2]/td[2]").textContent)
		assertEquals("Alex", byXPath("//tbody/tr[3]/td[2]").textContent)

		// sort descending
		click "Birthday"
		assertEquals("Alex", byXPath("//tbody/tr[1]/td[2]").textContent)
		assertEquals("Ilse", byXPath("//tbody/tr[2]/td[2]").textContent)
		assertEquals("Rob", byXPath("//tbody/tr[3]/td[2]").textContent)

		// sort by date time
		click "Date Created"
		assertEquals("Rob", byXPath("//tbody/tr[1]/td[2]").textContent)
		assertEquals("Ilse", byXPath("//tbody/tr[2]/td[2]").textContent)
		assertEquals("Alex", byXPath("//tbody/tr[3]/td[2]").textContent)

		// sort descending
		click "Date Created"
		assertEquals("Alex", byXPath("//tbody/tr[1]/td[2]").textContent)
		assertEquals("Ilse", byXPath("//tbody/tr[2]/td[2]").textContent)
		assertEquals("Rob", byXPath("//tbody/tr[3]/td[2]").textContent)
	}

	void assertTextByXPath(String expected, String xpath) {
		Element node = byXPath(xpath)
		assertEquals(expected, node.textContent)
	}
}