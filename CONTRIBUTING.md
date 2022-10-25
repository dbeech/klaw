# Contributing Guidelines

🎉 First off: Thank you for your interest in contributing to our project 🥳

Whether it's a bug report, new feature, correction, or additional documentation, we greatly value feedback and contributions from our community.

Please read through this document before submitting any issues or pull requests. This ensures all parties have all necessary information to respond to your bug report or contribution.


## ❤️ Code of Conduct 

This project has adopted the [Contributor Covenant Code of Conduct](CODE_OF_CONDUCT.md). Before contributing, please take the time to read our COC. Everyone participating is expected to uphold this code. Please report unacceptable behavior to us!

For more information see the [Code of Conduct FAQ](https://www.contributor-covenant.org/faq/).

## Guideline commit messages

We uses the [Conventional Commits](https://www.conventionalcommits.org/) specification. It helps us creating a helpful and explicit git history. 

### ℹ️ Semantic prefixes for commit messages

- **fix**: a commit that patches a bug.  
Example: `fix: Removes circular dependency` 

- **feat**: a commit that introduces a new feature.
Example: `feat: Add sorting to user list`

- **docs**: a commit that changes something related to documentation.
Example: `docs: Update contribution guide with guidelines for commit messages`

- **refactor**: a commit that refactors existing code.
Example: `refactor: Update footer from kafkawize.io to klaw-project.io`


### ✍️ Writing a great commit message

A "great" commit message enables others to gain more context about a code change. While the `diff` is telling you **what** has changes, the commit message can tell you **why** it has changed.  

For more information read this article: [How to Write a Git Commit Message](https://cbea.ms/git-commit/). We used it as a base for our rules.


#### Rules

```
<prefix>: <description>

[optional body]

```

**1. Add a short description as first line**
The first line (`<description>`) should be a short description of your change. Limit it to preferably 50 characters. It never should be longer than 72 characters.

**⛔️ Don't**

`Add CONTRIBUTING.md with first information about Code Of Conduct and a guideline for commit messages that includes our first rules and pattern we want to establish`

**️✅ Do**

`Add first iteration for contribution guide`

**2. Use the "imperative mood" in first line**
"Imperative mood" means you form a sentence as if you were giving a command. You can image your commit message completing the sentence _"If applied, this commit will... <do your change>"_.

**⛔️ Don't**
`fix: Removed the newline that caused a linting error`
-> _"If applied, this commit will_ removed the newline that caused a linting error"

**️✅ Do**
`fix: Remove the newline that caused a linting error`
-> _"If applied, this commit will_ remove the newline that caused a linting error"

**3. Separate your description with a new line from the body.**
If you add an body, add an empty line between your description to separate it from the body. This makes the message more readable. It also makes `git log --oneline` or `git shortlog` more useable.

**4. Use an optional body to explain why, not how.**
You don't need to explain the code! The commit message has a changeset that contains this information. Use the body to explain _why_ you made a change. Not every commit needs to have a body. Often the code change itself is explanation enough.  

**⛔️ Don't**
```
fix: Fix typo

Change "optoinal" "to optional"
```


**️✅ Do**
```fix: Remove word

Remove "just" from description, because it can make people feel inadequate. 
```

**5. Wrap your body at 72 characters**
Git does not wrap text, so you have to take care of margins. Editors and IDEs can help with that.