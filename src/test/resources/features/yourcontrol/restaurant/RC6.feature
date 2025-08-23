@RC6
@YOUCONTROL

Feature: Test

  Scenario: RC6, Parse yourcontrol site

    #Step 1
    When [yourcontrol] Login -> Open login page
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Type email "rick.corporative+6@gmail.com"
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Type password "139018"
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Click login button
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Home -> Close banner
    When Timeout 2 seconds
    #Step 3
    When [yourcontrol] Home -> Parse kved "I" announcements with query "Ресторан" on page 7 product 5