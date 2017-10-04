# Metalnx Development Workflow

Metalnx now uses GitFlow as the standard workflow for development. 

So, on the Metalnx repository there are now three branches: 

- `master`: branch that will have the history of versions for the whole project
- `development`: this branch has the current *state-of-art* of the project
   - for any new feature, there should be a branch specially for that 
   - this branch should be forked from the `development` branch
- `revision1`: used as a development branch before (legacy)
   - it should no longer get updates

For further information about GitFlow WorkFlow, check out this [article](https://www.atlassian.com/git/tutorials/comparing-workflows). Plus, there are many other good resources on GitFlow if you want to know more about that.
