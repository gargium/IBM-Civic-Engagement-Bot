from selenium import webdriver
from selenium.webdriver.common.keys import Keys
import selenium.webdriver.support.ui as ui
import time


botname = "IBM Civic Engagement Bot"
usr = ""    # Enter Facbeook Username (Email) Here
pwd = ""    # Enter Facebook Password Credentials Here
searchElemClass = "_58al"

driver = webdriver.Firefox()
# or you can use Chrome(executable_path="/usr/bin/chromedriver")
driver.get("https://www.messenger.com/")
wait = ui.WebDriverWait(driver, 10)
assert "Messenger" in driver.title
elem = driver.find_element_by_id("email")
elem.send_keys(usr)
elem = driver.find_element_by_id("pass")
elem.send_keys(pwd)
elem.send_keys(Keys.RETURN)

results = wait.until(lambda driver: driver.find_elements_by_class_name('_4kzu'))
for result in results:
    elem = driver.find_element_by_css_selector("input."+searchElemClass)
    elem.send_keys(botname)
    botlink = wait.until(lambda driver: driver.find_element_by_xpath("//div[text() = '%s']" % botname ))
    botlink.click()
    input_box = wait.until(lambda driver: driver.find_element_by_css_selector("._1mf._1mj"))
    input_box.click()
    input_box.send_keys("Reps: 580 Portola Plaza, Los Angeles, CA 90095")
    input_box.send_keys(u'\ue007')
    time.sleep(1)
    responses = driver.find_elements_by_css_selector("._3oh-._58nk")
    msg = responses[-1].get_attribute('innerHTML')
    print msg
    if (msg == "Your representatives are Ted Lieu (Democratic) and Elan Carr (Republican)."):
        print "All tests passed"

# driver.close()
