@BC3
@YOUCONTROL

Feature: Test

  Scenario: BC3, Parse yourcontrol site

    #Step 1
    When [yourcontrol] Login -> Open login page
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Type email "rick.corporative+21@gmail.com"
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Type password "555109"
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Click login button
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Home -> Close banner
    When Timeout 2 seconds
    #Step 3
    When [yourcontrol] Home -> Parse kved "I" announcements with query "Пекарня" on page 3 product 10