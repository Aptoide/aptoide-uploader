---
name: complete-ticket
description: Complete a ticket by transitioning it to Review status and creating a PR to dev. Use when finishing work on a ticket, creating a pull request, or marking work as done.
---

# Complete Ticket

Close the loop on implemented features.

## Pre-flight Checks

1. **Verify Branch**
   - Check current branch matches pattern `*/AND-<number>_*`
   - Extract ticket number from branch name

2. **Check Working Tree**
   - Run `git status`
   - Warn if uncommitted changes exist

3. **Verify Branch Status**
   - Check if branch is ahead of `dev`
   - Warn if behind or diverged

## Flow

1. **Summarize Changes**
   - Run `git diff dev...HEAD --stat`
   - List modified files with line counts
   - Generate technical summary of changes

2. **Transition Jira Ticket**
   - Get available transitions with `mcp__atlassian__getTransitionsForJiraIssue`
   - Find transition to "Review" status
   - Execute transition with technical summary as comment

3. **Create Pull Request**
   - Use `gh pr create` with:
     - Title: Jira ticket summary
     - Body: Technical summary + link to ticket
     - Base: `dev`

4. **Optional: Update Confluence**
   - If user requests, create/update feature documentation

5. **Update State**
   - Remove ticket from `activeTickets` in `.claude/state/tpo-context.json`

## Output

- Return PR URL
- Confirm Jira transition

## Guard Rails

- Never force push
- Never skip pre-commit hooks
- Always target `dev` branch

## Branch Naming

```
Pattern: <type>/AND-<number>_<title-with-hyphens>
Types: feature, bugfix, task
Example: feature/AND-631_improve-upload-notification
```

## Jira Config

- Project Key: AND
- Cloud ID: `995bf29c-64b4-4193-bfd8-06af41245a53`
- Target Status: Review
