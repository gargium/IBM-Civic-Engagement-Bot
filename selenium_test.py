from selenium import webdriver
from selenium.webdriver.common.keys import Keys
import selenium.webdriver.support.ui as ui
from selenium.webdriver.support.expected_conditions import \
        staleness_of
import time
import unittest

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
        response = self.getBotResponse("hello")
        print response
        valid_responses = ["Hello! How can I help you?", "Hi! How can I help you?", "Good Day! How can I help you?"]
        self.assertTrue(response in valid_responses, "Wrong response to 'hello'." )

    def test_clear_location(self):
        response = self.getBotResponse("clear location")
        valid_response = "Your location was cleared!"
        self.assertEqual(response, valid_response, "Wrong reponse to 'clear location'!")

    def test_polling_location_without_address(self):
        self.getBotResponse("clear location")
        response = self.getBotResponse("what's my polling location?")
        valid_response = "Please enter your address first, and retry your query."
        self.assertEqual(response, valid_response, "Wrong reponse to 'polling location' w/o address!")

    def test_polling_location_with_address(self):
        self.getBotResponse("443 Midvale Ave, Los Angeles, CA 90024")
        response = self.getBotResponse("what's my polling location?")
        self.assertTrue(response.startswith("Your polling location is "))

    @classmethod
    def tearDownClass(inst):
        # close the browser window
        # inst.driver.quit()
        return

if __name__ == "__main__":
    unittest.main()

# driver.close()
