# Contributing code to Skunkworks-Crow

This is a living document. If you see something that could be improved, edit this document and submit a pull request following the instructions below!

## Table of Contents
* [Submitting a pull request](#submitting-a-pull-request)
* [Making sure your pull request is accepted](#making-sure-your-pull-request-is-accepted)
* [The review process](#the-review-process)
* [Work in progress pull requests](#work-in-progress-pull-requests)
* [Code style guidelines](#code-style-guidelines)
* [Strings](#strings)
* [Code from external sources](#code-from-external-sources)

## Submitting a pull request
To contribute code to Skunkworks-Crow, you will need to open a [pull request](https://help.github.com/articles/about-pull-requests/) which will be reviewed by the community and then merged into the core project. Generally, a pull request is submitted when a unit of work is considered complete but it can sometimes be helpful to share ideas through a work in progress (WIP) pull request ([learn more](#work-in-progress-pull-requests)).

1. [Set up your development environment](https://github.com/opendatakit/skunkworks-crow#setting-up-your-development-environment).

1. To make sure you have the latest version of the code, set up this repository as [a remote for your fork](https://help.github.com/articles/configuring-a-remote-for-a-fork/) and then [sync your fork](https://help.github.com/articles/syncing-a-fork/).

1. Create a branch for the code you will be writing:

        git checkout -b NAME_OF_YOUR_BRANCH

1. If there is an [issue](https://github.com/opendatakit/skunkworks-crow/issues) corresponding to what you will work on, put `@opendatakit-bot claim` as a comment on issue to say you are claiming it. If this is your first time contributing to the repo, the bot will send you an invite. Once you accept this invite, the bot will assign you to the issue. If there is no issue yet, create one to provide background on the problem you are solving.

1. Once you've made incremental progress towards you goal, commit your changes with a meaningful commit message. Use [keywords for closing issues](https://help.github.com/articles/closing-issues-via-commit-messages/) to refer to issues and have them automatically close when your changes are merged.

        git commit -m "Do a thing. Fix #1."

1. Push changes to your fork at any time to make them publicly available:

        git push

1. When your changes are ready to be added to the core Skunkworks-Crow project, [open a pull request](https://help.github.com/articles/creating-a-pull-request/). Make sure to set the base fork to `opendatakit/skunkworks-crow`. Describe your changes in the comment, refer to any relevant issues using [keywords for closing issues](https://help.github.com/articles/closing-issues-via-commit-messages/) and tag any person you think might need to know about the changes.

1. Pull requests will be reviewed when committers have time. If you haven't received a review in 10 days, you may notify committers by putting `@opendatakit/skunkworks-crow` in a comment.

## Making sure your pull request is accepted
1. Confirm that your code compiles.

1. Verify the functionality. Ideally, include automated tests with each pull request. If that's not possible, describe in the pull request comment which cases you tried manually to confirm that your code works as expected. Attach a test form when appropriate. This form should only include questions which are useful for verifying your change.

1. Make sure that there is an issue that corresponds to the pull request and that it has been discussed by the community as necessary.

1. Keep your pull request focused on one narrow goal. This could mean addressing an issue with multiple, smaller pull requests. Small pull requests are easier to review and less likely to introduce bugs. If you would like to make stylistic changes to the code, create a separate pull request.

1. Run `./gradlew lint checksyle pmd findbugs` and fix any errors.

1. Write clear code. Use descriptive names and create meaningful abstractions (methods, classes).

1. Document your reasoning. Your commit messages should make it clear why each change has been made.

1. Point out decisions you made and what alternatives you considered. If you're unsure about a particular approach, ask a question to make your own thinking clear and help the reviewer identify controversial parts of the proposed solution. For example: "here I returned a result object to represent the status after the transaction. I also considered throwing an exception in case of error but I didn't like that it made it unclear where an error happened. Which do you prefer and why?" This is particularly important for [work in progress pull requests](#work-in-progress-pull-requests).

1. Follow the guidelines below.

## The review process
Bug fixes, pull requests corresponding to issues with a clearly stated goal and pull requests with clear tests and/or process for manual verification are given priority. Pull requests that are unclear or controversial may be tagged as `needs discussion` and/or may take longer to review.

We try to have at least two people review every pull request and we encourage everyone to participate in the review process to get familiar with the code base and help ensure higher quality. Reviewers should ask themselves some or all of the following questions:
- Was this change adequately discussed prior to implementation?
- Is the intended behavior clear under all conditions?
- What interesting cases should be verified?
- Is the behavior as intended in all cases?
- What other functionality could this PR affect? Does that functionality still work as intended?
- Was the change verified with several different devices and Android versions?
- Is the code easy to understand and to maintain?
- Is there sufficient detail to inform any changes to documentation?

When a pull request is first created, @opendatakit-bot tags it as `needs review` to indicate that code review is needed. Community members review the code and leave their comments, verifying that the changes included are relevant and properly address the issue. A maintainer does a thorough code review and when satisfied with the code, tags the pull request as `needs testing` to indicate the need for a manual [black-box testing](https://en.wikipedia.org/wiki/Black-box_testing) pass. A pull request may go back and forth between `needs testing` and `needs review` until the behavior is thoroughly verified. Once the behavior has been thoroughly verified, the pull request is tagged as `behavior verified`. A maintainer then merges the changes. Pull requests that need more complete reviews including review of approach and/or appropriateness are tagged with `reviews wanted`. Any community member is encouraged to participate in the review process!

Small fixes that target very particular bugs may occasionally be merged without a second review.

## Work in progress pull requests

Work in progress (WIP) pull requests are useful to illustrate a proposed direction and get early feedback before committing to a solution direction.

Work in progress pull requests:
- Should include `[WIP]` in front of the pull request title.
- Should specifically describe the proposed solution and feedback wanted.
- Will not be merged until you remove `[WIP]` from the title.

Pull request can be raised in draft mode directly by chosing an option from the drop down that is available while making pull request. Read more about [Draft pull requests](https://help.github.com/en/articles/creating-a-pull-request).

## Code style guidelines
Follow the [Android style rules](http://source.android.com/source/code-style.html) and the [Google Java style guide](https://google.github.io/styleguide/javaguide.html).

## Strings
Always use [string resources](https://developer.android.com/guide/topics/resources/string-resource.html) instead of literal strings. This ensures wording consistency across the project and also enables full translation of the app. Only make changes to the base `res/values/strings.xml` English file and not to the other language files.

## Code from external sources
Skunkworks-crow is released under the [Apache 2.0 license](https://www.apache.org/licenses/LICENSE-2.0). Please make sure that any code you include is an OSI-approved [permissive license](https://opensource.org/faq#permissive). **Please note that if no license is specified for a piece of code or if it has an incompatible license such as GPL, using it puts the project at legal risk**.

Sites with compatible licenses (including [StackOverflow](http://stackoverflow.com/)) will sometimes provide exactly the code snippet needed to solve a problem. You are encouraged to use such snippets in Skunkworks-crow as long as you attribute them by including a direct link to the source. In addition to complying with the content license, this provides useful context for anyone reading the code.
