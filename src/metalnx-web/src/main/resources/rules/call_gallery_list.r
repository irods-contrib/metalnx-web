galleryBrowse {
#Input parameters are:
#  logical_path
#  offset
#  length
#  
#Output parameter is:
#  String (a JSON array of results)
   irods_policy_list_thumbnails_for_logical_path("/tempZone/home/rods","0","100", *out);
}
INPUT null
OUTPUT *out