<template>
  <div id="app">
    <div class="container-fluid">
      <Notifications></Notifications>
    </div>
  </div>
</template>

<script>
import Notifications from './components/Notifications'

export default {
  name: 'App',
  components: {
    Notifications
  }
}
</script>