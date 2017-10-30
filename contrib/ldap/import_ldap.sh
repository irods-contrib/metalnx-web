#!/bin/sh

CONFFILE=./import_ldap.conf
HELP=false

#============================================================================
# Function to print out the usage
print_usage()
{
  echo ""
  echo "Usage: $0 [-f conf_file_name]"
  echo "       -f     -- Specifies which configuration file must  be taken."
  echo 
  echo "If no configuration file is specified, the script will look for the import_ldap.conf file."
  echo
  echo "       Example :  $0 -f linux_users.conf"
  echo "         or       $0 -f admin_users.conf"
  echo "         or       $0"
  echo ""
  exit 2
}

#============================================================================
# Parse any options arguments passed to the script
while getopts "hf:" OPTION; do
  case $OPTION in
    h) HELP=true
       ;;
    f) CUSTOM_CONFFILE=$OPTARG
       ;;
  esac
done

if [ $HELP == true ];
then
    print_usage
fi

#============================================================================
# Checking custom file argument
if [ ! -z "$CUSTOM_CONFFILE" ];
then
    if [ ! -f $CUSTOM_CONFFILE ];
    then
        echo "File $CUSTOM_CONFFILE does not exist. Exiting..."
	exit 1 
    fi
    echo "Using $CUSTOM_CONFFILE as input configuration file."
    CONFFILE=$CUSTOM_CONFFILE
else
    echo "No input conf file has been scpeficied. Using import_ldap.conf."
fi

#============================================================================
# Importing settings from configuration file and displaying them
. ./$CONFFILE

echo
echo "Base Domain Name: $BASEDN"
echo "Bind Domain Name: $BINDDN"
echo "Look for user in group: $GROUPDN"
echo "Creating iRODS user type: $USERTYPE"
echo

#============================================================================
# Loading data from LDAP server
RESULTS=$(ldapsearch -x -b "$BASEDN" -s sub -D "$BINDDN" -W "(&(objectCategory=user)(memberOf=$GROUPDN))" | grep sAMAccountName | cut -d':' -f 2)

TOTAL=0
COUNT=0

#============================================================================
# Creating iRODS users
for username in $RESULTS;
do
    echo "Checking user [$username]..."
    iadmin lu $username | grep "No rows found" > /dev/null
    if [[ $? -eq 0 ]]; then
        echo "User [$username] does not exist in iRODS. Creating it..."
        iadmin mkuser $username $USERTYPE
        COUNT=$(expr $COUNT + 1)
    else
        echo "User [$username] already on iRODS."
    fi
    TOTAL=$(expr $TOTAL + 1)
    echo
done

#============================================================================
# Printing summary
echo
echo "Summary"
echo "=========================="
echo -e "Users imported:\t\t$COUNT"
echo -e "Total users:\t\t$TOTAL"

exit 0
