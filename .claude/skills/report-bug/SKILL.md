---
name: report-bug
description: Create a detailed Jira bug report with developer assistance directive. Investigates codebase, identifies likely root cause, and generates a prompt to help devs fix the issue. Use when reporting bugs, creating bug tickets, or investigating issues.
---

# Report Product Bug

Create detailed bug report with developer assistance.

## Flow

1. **Gather Bug Details**
   - Ask for: symptom, expected behavior, actual behavior
   - Ask for: repro steps if known

2. **Investigate Codebase**
   - Search for related code using Grep
   - Check recent commits with `git log --oneline -20`
   - Identify likely root cause location

3. **Generate Bug Report**

```markdown
## Summary
[One line description]

## Environment
- App Version: [from build.gradle]
- Android: [if known]

## Steps to Reproduce
1. [Step 1]
2. [Step 2]
3. [Step 3]

## Expected Behavior
[What should happen]

## Actual Behavior
[What actually happens]

## Technical Analysis
- Likely Location: [file:line]
- Related Code: [brief explanation]
- Hypothesis: [what might be wrong]

## Developer Assistant Directive
```
You are debugging AND-XXX in the Aptoide Uploader codebase.

The bug manifests as: [symptom]

Start by examining:
- [file:line] - [why]

Likely cause: [hypothesis]

To verify, run: ./gradlew test --tests "TestClass"

Suggested fix approach: [brief guidance]
```
```

4. **Create Jira Bug**
   - Use `mcp__atlassian__createJiraIssue` with issueTypeName: `Bug`
   - Project: AND
   - Title: `[Uploader]: <bug title>`
   - Include Developer Assistant Directive in description

5. **Update State**
   - Add bug key to `activeTickets` in `.claude/state/tpo-context.json`

## Output

- Return Jira bug URL
- Display the Developer Assistant Directive for easy copy

## Jira Config

- Project Key: AND
- Cloud ID: `995bf29c-64b4-4193-bfd8-06af41245a53`
- Issue Type: Bug (ID: 1)
