<template>
  <b-container class="sm-left">
    <b-row class="text-sm-left">
      <b-table striped head-variant="dark" hover :items="getNotifications.items" :fields="getNotifications.fields" >
    </b-table>
  </b-row>
  </b-container>
</template>

<script>

export default {
    name: 'NTable',
    props: ['results'],
    computed: {
        getNotifications: function () {
        var resultData = [];
         for (var propt in this.results) {
        resultData.push(this.results[propt])
      }
      var data =  {
      fields: [
          {
            key: 'id',
            label: 'NotificationID',
            formatter: value => {
              return value
            }
          },
          {
            key: 'uuid',
            label: 'uuid',
            formatter: value => {
              return value
            }
          },
          {
            key: 'user_id',
            label: 'User ID',
            formatter: value => {
              return value
            }
          },
          {
            key: 'subject',
            label: 'subject',
            formatter: value => {
              return value
            }
          },
          {
            key: 'date_created',
            label: 'Date',
            formatter: value => {
              return value
            }
          },
          {
            key: 'message',
            label: 'message',
            formatter: value => {
              return value
            }
          }
        ],
        
        items: resultData
      }
      return data;
    }
    }
}
</script>
