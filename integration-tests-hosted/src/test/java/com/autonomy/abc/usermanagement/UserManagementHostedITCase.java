package com.autonomy.abc.usermanagement;

import com.autonomy.abc.config.TestConfig;
import com.autonomy.abc.selenium.config.ApplicationType;
import com.autonomy.abc.selenium.config.HSOApplication;
import com.autonomy.abc.selenium.element.FormInput;
import com.autonomy.abc.selenium.element.GritterNotice;
import com.autonomy.abc.selenium.page.HSOElementFactory;
import com.autonomy.abc.selenium.page.admin.HSOUsersPage;
import com.autonomy.abc.selenium.page.login.FindHasLoggedIn;
import com.autonomy.abc.selenium.users.*;
import com.autonomy.abc.topnavbar.on_prem_options.UsersPageTestBase;
import com.hp.autonomy.frontend.selenium.element.ModalView;
import com.hp.autonomy.frontend.selenium.sso.GoogleAuth;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.autonomy.abc.framework.ABCAssert.assertThat;
import static com.autonomy.abc.framework.ABCAssert.verifyThat;
import static com.autonomy.abc.matchers.ElementMatchers.*;
import static com.autonomy.abc.selenium.users.GMailHelper.gmailString;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.junit.Assert.fail;

public class UserManagementHostedITCase extends UsersPageTestBase {

    private HSOUserService userService;
    private HSOUsersPage usersPage;
    private final static Logger LOGGER = LoggerFactory.getLogger(UserManagementHostedITCase.class);

    public UserManagementHostedITCase(TestConfig config, String browser, ApplicationType type, Platform platform) {
        super(config, browser, type, platform);
    }

    @Before
    public void hostedSetUp(){
        userService = ((HSOApplication) getApplication()).createUserService(getElementFactory());
        usersPage = ((HSOElementFactory) getElementFactory()).getUsersPage();
    }

    @Test
    public void testCannotAddInvalidEmail(){
        HSONewUser newUser = new HSONewUser("jeremy","jeremy");

        usersPage.createUserButton().click();

        try {
            newUser.signUpAs(Role.ADMIN, usersPage, config.getWebDriverFactory());
        } catch (TimeoutException | HSONewUser.UserNotCreatedException e){ /* Expected behaviour */ }

        verifyThat(getContainingDiv(usersPage.getUsernameInput()), not(hasClass("has-error")));
        verifyThat(getContainingDiv(usersPage.getEmailInput()), not(hasClass("has-error")));
        verifyThat(getContainingDiv(usersPage.getUserLevelDropdown()), not(hasClass("has-error")));
        verifyThat(getContainingDiv(usersPage.createButton()), not(hasClass("has-error")));

        verifyThat(ModalView.getVisibleModalView(getDriver()).getText(), containsString("Error! New user profile creation failed."));

        usersPage.closeModal();

        usersPage.refreshButton().click();
        usersPage.loadOrFadeWait();

        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));

        //Sometimes it requires us to add a valid user before invalid users show up
        userService.createNewUser(new HSONewUser("Valid", gmailString("NonInvalidEmail")), Role.ADMIN, config.getWebDriverFactory());

        usersPage.refreshButton().click();
        usersPage.loadOrFadeWait();

        verifyThat(usersPage.getUsernames(), not(hasItem(newUser.getUsername())));
    }

    @Test
    public void testResettingAuthentication(){
        HSONewUser newUser = new HSONewUser("resettingauthenticationtest",gmailString("resetauthtest")).authenticate();

        final HSOUser user = userService.createNewUser(newUser,Role.USER, config.getWebDriverFactory());

        waitForUserConfirmed(user);

        userService.resetAuthentication(user);

        verifyThat(usersPage.getText(), containsString("Done! A reset authentication email has been sent to " + user.getUsername()));

        WebDriver driver = config.createWebDriver();

        try {
            new Thread(){
                @Override
                public void run() {
                    new WebDriverWait(getDriver(),180)
                            .withMessage("User never reset their authentication")
                            .until(GritterNotice.notificationContaining("User " + user.getUsername() + " reset their authentication"));

                    LOGGER.info("User reset their authentication notification shown");
                }
            }.start();
            user.resetAuthentication(driver);
        } finally {
            for(String browserHandle : driver.getWindowHandles()){
                driver.switchTo().window(browserHandle);
                driver.close();
            }
        }
    }

    @Test
    public void testEditingUsername(){
        User user = userService.createNewUser(new HSONewUser("editUsername", gmailString("editUsername")), Role.ADMIN, config.getWebDriverFactory());

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        userService.editUsername(user, "Dave");

        verifyThat(usersPage.getUsernames(), hasItem(user.getUsername()));

        try {
            userService.editUsername(user, "");
        } catch (TimeoutException e) { /* Should fail here as you're giving it an invalid username */ }

        verifyThat(usersPage.editUsernameInput(user).getElement().isDisplayed(), is(true));
        verifyThat(usersPage.editUsernameInput(user).getElement().findElement(By.xpath("./../..")), hasClass("has-error"));
    }

    @Test
    public void testAddingAndAuthenticatingUser(){
        final User user = userService.createNewUser(new HSONewUser("authenticatetest", gmailString("authenticationtest")).authenticate(),
                Role.USER, config.getWebDriverFactory());

        waitForUserConfirmed(user);

        verifyThat(usersPage.getStatusOf(user), is(Status.CONFIRMED));
    }

    @Test
    public void testCreateUser(){
        usersPage.createUserButton().click();
        assertThat(usersPage, modalIsDisplayed());
        final ModalView newUserModal = ModalView.getVisibleModalView(getDriver());
        verifyThat(newUserModal, hasTextThat(startsWith("Create New Users")));

        usersPage.createButton().click();
        verifyThat(newUserModal, containsText("Error! Email address must not be blank"));

        String username = "Andrew";

        usersPage.addUsername(username);
        usersPage.clearEmail();
        usersPage.createButton().click();
        verifyThat(newUserModal, containsText("Error! Email address must not be blank"));

        usersPage.getEmailInput().setValue("hodtestqa401+CreateUserTest@gmail.com");
        usersPage.selectRole(Role.USER);
        usersPage.createButton().click();
//        verifyThat(newUserModal, containsText("Done! User Andrew successfully created"));

        usersPage.closeModal();
        verifyThat(usersPage, not(containsText("Create New Users")));   //Not sure what this is meant to be doing? Verifying modal no longer open??

        //CSA-1766
        verifyThat(usersPage.getUsernames(),hasItem(username));
    }

    @Test
    public void testLogOutAndLogInWithNewUser() {
        HSOUser user = userService.createNewUser(new HSONewUser("YouTestYourLoggingIn", gmailString("YourLoggingOut"),
                new GoogleAuth("hodtestqa401@gmail.com", "qoxntlozubjaamyszerfk")).authenticate(), Role.ADMIN, config.getWebDriverFactory());

        logout();

        getDriver().get(config.getFindUrl());
        loginAs(user);

        if(!new FindHasLoggedIn((HSOElementFactory) getElementFactory()).hasLoggedIn()){
            fail("Haven't been logged in to find");
        }
    }

    @Test
    public void testAddStupidlyLongUsername() {
        final String longUsername = StringUtils.repeat("a", 100);

        User user = userService.createNewUser(new HSONewUser(longUsername, "hodtestqa401+longusername@gmail.com"), Role.ADMIN, config.getWebDriverFactory());
        assertThat(usersPage.getTable(), containsText(longUsername));
        userService.deleteUser(user);

        assertThat(usersPage.getTable(), not(containsText(longUsername)));
    }

    private void waitForUserConfirmed(User user){
        new WebDriverWait(getDriver(),60).pollingEvery(2, TimeUnit.SECONDS).withMessage("User not showing as confirmed").until(new WaitForUserToBeConfirmed(user));
    }

    private class WaitForUserToBeConfirmed implements ExpectedCondition<Boolean>{
        private final User user;

        WaitForUserToBeConfirmed(User user){
            this.user = user;
        }

        @Override
        public Boolean apply(WebDriver driver) {
            usersPage.refreshButton().click();

            return usersPage.getStatusOf(user).equals(Status.CONFIRMED);
        }
    }

    private WebElement getContainingDiv(WebElement webElement){
        return webElement.findElement(By.xpath(".//../.."));
    }

    private WebElement getContainingDiv(FormInput formInput){
        return getContainingDiv(formInput.getElement());
    }
}
