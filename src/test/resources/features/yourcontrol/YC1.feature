@YC1

Feature: Test

  Scenario: YC1, Parse yourcontrol site

    #Step 1
    When [yourcontrol] Home -> Open home page
    When Timeout 5 seconds
    #Step 2
    When [yourcontrol] Home -> Type "Пекарня" into input
    When Timeout 2 seconds
    #Step 3
    When [yourcontrol] Home -> Click search button
    When Timeout 2 seconds
    #Step 3
    When [yourcontrol] Home -> Parse announcements