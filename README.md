# CSE 360 Team Project Phase 2

## Overview
Phase 2 builds on a Discussion Board Q&A system within a JavaFX application. Students can ask questions, provide answers, and interact with replies. The system supports search, filtering, and marking answers as correct to resolve questions. Permissions are role-based: students can only edit/delete their own posts, while admins have full control.

## Key Changes
- **Discussion Board**: Entire discussion board module with database access object.
- **Student Role**: Added a Student class to mimic user function but allow for expansion of student specific functions
- **Question and Answer Features**: Students can create questions which will be displayed on the discussion board, other users can respond with answers and feedback.
- **Replies**: Added ability to add, edit, or delete replies to answers for clarification.
- **Search and Filter**: Implemented keyword search and filters (e.g answered).
- **Correct Answer Marking**: Students can mark an answer as correct to resolve questions.
- **Testing**: Included screencasts with manual tests for various scenarios.

## New User Stories
- Students can ask questions and receive a curated list of potential answers from sources I trust so I can quickly get back to work.
- Students can see a list of questions others have asked that might be related to a question I am about to ask.
- Students can see their own list of unresolved questions and the number of unread potential answers received.
- Students can see all unresolved questions, and view all answers. Can propose a new potential answer.
- Students can specify that an answer resolved their question.
- Student can provide a comment as feedback.
- Students can read from a list of recently asked questions and provide private feedback to the student who asked the question.
- Student can produce a new question or answer based on the feedback.
- Student can search to find specific questions  that have resolved answers.
- Students can search for both answered and unanswered questions, as well as view reviewer suggestions to curate the best results.
