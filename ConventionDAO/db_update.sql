CREATE TABLE `table_pk_key` (
  `id` bigint(20) unsigned NOT NULL auto_increment,
  `table_name` varchar(32) NOT NULL default '',
  PRIMARY KEY  (`id`),
  UNIQUE KEY `table_name` (`table_name`)
) ENGINE=MyISAM;