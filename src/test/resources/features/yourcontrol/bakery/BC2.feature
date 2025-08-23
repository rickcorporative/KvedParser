@BC2
@YOUCONTROL

Feature: Test

  Scenario: BC2, Parse yourcontrol site

    #Step 1
    When [yourcontrol] Login -> Open login page
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Type email "rick.corporative+20@gmail.com"
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Type password "660049"
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Login -> Click login button
    When Timeout 2 seconds
    #Step
    When [yourcontrol] Home -> Close banner
    When Timeout 2 seconds
    #Step 3
    When [yourcontrol] Home -> Parse kved "I" announcements with query "Пекарня" on page 2 product 5