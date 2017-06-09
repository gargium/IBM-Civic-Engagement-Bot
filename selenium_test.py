from selenium import webdriver
from selenium.webdriver.common.keys import Keys
import selenium.webdriver.support.ui as ui
from selenium.webdriver.support.expected_conditions import \
        staleness_of
import time
import unittest
import random

botname = "IBM Civic Engagement Bot"
usr = ""    # Enter Facbeook Username (Email) Here
pwd = ""    # Enter Facebook Password Credentials Here
searchElemClass = "_58al"


class ChatBotTestCase(unittest.TestCase):
    @classmethod
    def setUpClass(inst):
        inst.driver = webdriver.Firefox()
        # or you can use Chrome(executable_path="/usr/bin/chromedriver")
        inst.driver.get("https://www.messenger.com/")
        wait = ui.WebDriverWait(inst.driver, 10)
        assert "Messenger" in inst.driver.title
        elem = inst.driver.find_element_by_id("email")
        elem.send_keys(usr)
        elem = inst.driver.find_element_by_id("pass")
        elem.send_keys(pwd)
        elem.send_keys(Keys.RETURN)

        results = wait.until(lambda driver: driver.find_elements_by_class_name('_4kzu')) #logged in
        elem = inst.driver.find_element_by_css_selector("input."+searchElemClass)
        elem.send_keys(botname)
        # click on bot conversation
        botlink = wait.until(lambda driver: driver.find_element_by_xpath("//div[text() = '%s']" % botname ))
        botlink.click()
        inst.input_box = wait.until(lambda driver: driver.find_element_by_css_selector("._1mf._1mj"))
        # chat with bot is active

    def getBotResponse(self, input):
        wait = ui.WebDriverWait(self.driver, 1)
        self.input_box = wait.until(lambda driver: driver.find_element_by_css_selector("._1mf._1mj"))
        self.input_box.click()
        self.input_box.send_keys(input)
        self.input_box.send_keys(u'\ue007')
        # wait = ui.WebDriverWait(self.driver, 10).until(
        #     staleness_of(self.driver.find_elements_by_css_selector("._3oh-._58nk")[-1])
        # )
        time.sleep(6)
        responses = self.driver.find_elements_by_css_selector("._3oh-._58nk")
        response = responses[-1].get_attribute('innerHTML')
        return response.encode('ascii','ignore')

    def test_hello(self):
        print "Selecting randomized greeting."
        print "Testing basic greeting."
        response = self.getBotResponse("hello")
        valid_responses = ["Hello! How can I help you?", "Hi! How can I help you?", "Good Day! How can I help you?"]
        self.assertTrue(response in valid_responses, "Wrong response to 'hello'." )

    def test_clear_location(self):
        print "Clearing location."
        response = self.getBotResponse("clear location")
        valid_response = "Your location was cleared!"
        self.assertEqual(response, valid_response, "Wrong reponse to 'clear location'!")

    def test_polling_location_without_address(self):
        print "Testing polling location without address."
        self.getBotResponse("clear location")
        response = self.getBotResponse("what's my polling location?")
        valid_response = "Please enter your address first, and retry your query."
        self.assertEqual(response, valid_response, "Wrong reponse to 'polling location' w/o address!")

    def test_polling_location_with_address(self):
        print "Testing polling location."
        self.getBotResponse("443 Midvale Ave, Los Angeles, CA 90024")
        response = self.getBotResponse("what's my polling location?")
        self.assertTrue(response.startswith("Your polling location is "))

    def test_bills(self):
        print "Testing bills (1/2) : SOPA"
        print "Testing bills (2/2) : National Firearms Act"
        response = self.getBotResponse("Tell me about SOPA")
        self.assertTrue(response.startswith("The Stop Online Piracy Act (SOPA) was a controversial United States bill"))
        response = self.getBotResponse("Give me information on National Firearms Act")
        self.assertTrue("imposes a statutory excise tax on the manufacture and transfer of certain firearms" in response)

    def test_politicians_info(self):
        queryList = ["Give me info on", "Give me information on", "Tell me about"]
        responseMap = {
                    'Donald Trump': '45th and current President of the United States',
                    'Mike Pence': '48th and current Vice President of the United States',
                    'Jerry Brown': '39th Governor of California since 2011',
                    'Dianne Feinstein': 'senior United States Senator from California',
                    'Darrell Issa': 'Republican U.S. Representative for California\'s 49th congressional district',
                    'John McCain': 'senior United States Senator from Arizona',
                    'Kathy Hochul': '77th and current Lieutenant Governor of the State of New York'
                    }
        for i, key in enumerate(responseMap.keys()):
            print "Testing politicians (%d/%d) : '%s'" % (i, len(responseMap), key)
            response = self.getBotResponse(random.choice(queryList) + " " + key)
            self.assertTrue(responseMap[key] in response)


    @classmethod
    def tearDownClass(inst):
        # close the browser window
        # inst.driver.quit()
        return

if __name__ == "__main__":
    unittest.main()

# driver.close()
