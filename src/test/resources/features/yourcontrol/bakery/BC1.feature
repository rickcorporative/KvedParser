@BC1
@YOUCONTROL

Feature: Test

  Scenario: BC1, Parse yourcontrol site

    #Step 1
    When [yourcontrol] Login -> Open login page
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Type email "rick.corporative+19@gmail.com"
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Type password "290189"
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Click login button
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Home -> Close banner
    When Timeout 2 seconds
    #Step 3
    When [yourcontrol] Home -> Parse kved "I" announcements with query "Пекарня" on page 1 product 0