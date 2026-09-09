-- Later upstream MySQL migrations are disabled in H2 mode. Seed their source
-- settings without overwriting existing user configuration or credentials.
INSERT INTO sq_config (config_name, config_value, config_key, config_type, config_show, config_null_check, config_disabled)
SELECT '开启咪咕音乐插件', 'true', 'plug.mg.open', 'boolean', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sq_config WHERE config_key = 'plug.mg.open');

INSERT INTO sq_config (config_name, config_value, config_key, config_type, config_show, config_null_check, config_disabled)
SELECT '开启Tidal音乐插件', 'true', 'plug.tidal.open', 'boolean', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sq_config WHERE config_key = 'plug.tidal.open');

INSERT INTO sq_config (config_name, config_value, config_key, config_type, config_show, config_null_check, config_disabled)
SELECT 'Tidal访问token', '', 'plug.tidal.token', 'input', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sq_config WHERE config_key = 'plug.tidal.token');

-- Keep the original SQMusic library layout: artist/album/song, with metadata
-- files written beside the audio file. The root itself is supplied by
-- system.download.path (mounted as /music in the container).
INSERT INTO sq_config (config_name, config_value, config_key, config_type, config_show, config_null_check, config_disabled)
SELECT '下载歌曲文件名模板', CONCAT('$', '{artists}/', '$', '{album}/', '$', '{musicName} - ', '$', '{artists}'), 'system.download.file.template', 'input', 1, 1, 0
WHERE NOT EXISTS (SELECT 1 FROM sq_config WHERE config_key = 'system.download.file.template');
