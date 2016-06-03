#!/bin/sh

CONFFILE=./populate_ldap.conf
DATAFILE=./populate.data.csv
TEMPLATE_FILE=./populate.template
HELP=false

#============================================================================
# Function to print out the usage
print_usage()
{
  echo ""
  echo "Usage: $0 [-d data_file] [-f conf_file_name]"
  echo "       -f     -- Specifies which configuration file must  be taken."
  echo "       -d     -- Specifies the input data file."
  echo
  echo "If no configuration file is specified, the script will look for the populate_ldap.conf file."
  echo
  exit 2
}

#============================================================================
# Parse any options arguments passed to the script
while getopts "hf:d:" OPTION; do
  case $OPTION in
    h) HELP=true
       ;;
    d) DATAFILE=$OPTARG
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
    echo "No input conf file has been scpeficied. Using populate_ldap.conf."
fi

. ./$CONFFILE

#============================================================================
# Preparing common variables
DOMAIN_NAME_1=$(echo $DOMAIN | cut -d'.' -f1)
DOMAIN_NAME_2=$(echo $DOMAIN | cut -d'.' -f2)
BINDDN="cn=$BIND_USER,cn=Users,dc=$DOMAIN_NAME_1,dc=$DOMAIN_NAME_2"

# Reading user password
echo -n "Enter \"$BIND_USER\" password on LDAP server: "
read -s BIND_PW
echo

GROUPDN="cn=$GROUP_NAME,cn=Users,dc=$DOMAIN_NAME_1,dc=$DOMAIN_NAME_2"

#============================================================================
# Reading data file
if [ ! -f $DATAFILE ];
then
    echo "Data file is invalid. Make sure it exists before retrying."
    exit 2
fi

if [ ! -f $TEMPLATE_FILE ];
then
    echo "Template file for user creation does not exist."
    exit 2
fi

while read line;
do
    USER_FULLNAME=$(echo $line | cut -d';' -f1)
    USER_FIRSTNAME=$(echo $USER_FULLNAME | cut -d' ' -f1)
    USER_LASTNAME=$(echo $USER_FULLNAME | cut -d' ' -f2)
    USER_USERNAME=$(echo $line | cut -d';' -f2)
    USER_PASSWORD=$(echo $line | cut -d';' -f3)

    USER_DN="CN=$USER_FULLNAME,CN=Users,DC=$DOMAIN_NAME_1,DC=$DOMAIN_NAME_2"
    USER_PRINCIPAL_NAME="$USER_USERNAME@$DOMAIN"
    UNIX_HOME="/home/$USER_USERNAME"

    # Making sure the user template file exists
    USER_TEMPLATE_FILE="$USER_USERNAME.add"
    cp $TEMPLATE_FILE $USER_TEMPLATE_FILE

    sed -i "s|:ldap_dn:|$USER_DN|g" $USER_TEMPLATE_FILE
    sed -i "s|:user_cn:|$USER_FULLNAME|g" $USER_TEMPLATE_FILE
    sed -i "s|:user_sn:|$USER_LASTNAME|g" $USER_TEMPLATE_FILE
    sed -i "s|:user_given_name:|$USER_FIRSTNAME|g" $USER_TEMPLATE_FILE
    sed -i "s|:distinguished_name:|$USER_DN|g" $USER_TEMPLATE_FILE
    sed -i "s|:user_username:|$USER_USERNAME|g" $USER_TEMPLATE_FILE
    sed -i "s|:user_userpassword:|$USER_PASSWORD|g" $USER_TEMPLATE_FILE
    sed -i "s|:user_principal_name:|$USER_PRINCIPAL_NAME|g" $USER_TEMPLATE_FILE
    sed -i "s|:domain_name:|$DOMAIN_NAME_1|g" $USER_TEMPLATE_FILE

    echo -n "Creating user on the LDAP server... "
    ldapadd -x -D $BINDDN -w $BIND_PW -f $USER_TEMPLATE_FILE 1> /dev/null 2>&1

    if [ $? -eq 0 ];
    then
        echo "Done!"
    else
	echo
        echo "User [$USER_USERNAME] is already created on the LDAP server. Skipping."
    fi

    echo "Removing user template file."
    rm $USER_TEMPLATE_FILE

    USER_GROUP_ADD="$USER_USERNAME.group"
    echo "" > $USER_GROUP_ADD
    echo "dn: $GROUPDN" >> $USER_GROUP_ADD
    echo "changetype: modify" >> $USER_GROUP_ADD
    echo "add: member" >> $USER_GROUP_ADD
    echo "member: $USER_DN"  >> $USER_GROUP_ADD

    echo -n "Assigning user to group..."
    ldapmodify -D $BINDDN -w $BIND_PW -f $USER_GROUP_ADD 1> /dev/null 2>&1
    if [ $? -eq 0 ];
    then
        echo "Done!"
    else
        echo
        echo "User [$USER_USERNAME] is already on group [$GROUP_NAME]. Skipping."
    fi

    echo "Removing group add modification file."
    rm $USER_GROUP_ADD

done <$DATAFILE
