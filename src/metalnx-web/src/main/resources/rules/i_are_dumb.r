galleryBrowse {
#Input parameters are:
#  logical_path
#  offset
#  length
#  
#Output parameter is:
#  String (a JSON array of results)
   irods_policy_list_thumbnails_for_logical_path(*logical_path,*offset,*limit, *out);
}
INPUT *logical_path="/",*offset="0",*limit="100"
OUTPUT *out