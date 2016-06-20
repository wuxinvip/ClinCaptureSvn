--Drop Table

DROP TABLE IF EXISTS archived_dataset_file CASCADE;
DROP TABLE IF EXISTS audit_event CASCADE;
DROP TABLE IF EXISTS audit_event_context CASCADE;
DROP TABLE IF EXISTS audit_event_values CASCADE;
DROP TABLE IF EXISTS audit_log_event CASCADE;
DROP TABLE IF EXISTS audit_log_event_type CASCADE;
DROP TABLE IF EXISTS audit_log_randomization CASCADE;
DROP TABLE IF EXISTS audit_user_login CASCADE;
DROP TABLE IF EXISTS authorities CASCADE;
DROP TABLE IF EXISTS coded_item CASCADE;
DROP TABLE IF EXISTS coded_item_element CASCADE;
DROP TABLE IF EXISTS completion_status CASCADE;
DROP TABLE IF EXISTS "configuration";
DROP TABLE IF EXISTS crf CASCADE;
DROP TABLE IF EXISTS crf_version CASCADE;
DROP TABLE IF EXISTS crfs_masking CASCADE;
DROP TABLE IF EXISTS databasechangelog CASCADE;
DROP TABLE IF EXISTS databasechangeloglock CASCADE;
DROP TABLE IF EXISTS dataset CASCADE;
DROP TABLE IF EXISTS dataset_crf_version_map CASCADE;
DROP TABLE IF EXISTS dataset_item_status CASCADE;
DROP TABLE IF EXISTS dataset_study_group_class_map CASCADE;
DROP TABLE IF EXISTS "dictionary";
DROP TABLE IF EXISTS discrepancy_description CASCADE;
DROP TABLE IF EXISTS discrepancy_note CASCADE;
DROP TABLE IF EXISTS discrepancy_note_type CASCADE;
DROP TABLE IF EXISTS dn_event_crf_map CASCADE;
DROP TABLE IF EXISTS dn_item_data_map CASCADE;
DROP TABLE IF EXISTS dn_study_event_map CASCADE;
DROP TABLE IF EXISTS dn_study_subject_map CASCADE;
DROP TABLE IF EXISTS dn_subject_map CASCADE;
DROP TABLE IF EXISTS dyn_item_form_metadata CASCADE;
DROP TABLE IF EXISTS dyn_item_group_metadata CASCADE;
DROP TABLE IF EXISTS dynamic_event CASCADE;
DROP TABLE IF EXISTS edc_item_metadata CASCADE;
DROP TABLE IF EXISTS event_crf CASCADE;
DROP TABLE IF EXISTS event_crf_section CASCADE;
DROP TABLE IF EXISTS event_definition_crf CASCADE;
DROP TABLE IF EXISTS export_format CASCADE;
DROP TABLE IF EXISTS group_class_types CASCADE;
DROP TABLE IF EXISTS item CASCADE;
DROP TABLE IF EXISTS item_data CASCADE;
DROP TABLE IF EXISTS item_data_type CASCADE;
DROP TABLE IF EXISTS item_form_metadata CASCADE;
DROP TABLE IF EXISTS item_group CASCADE;
DROP TABLE IF EXISTS item_group_metadata CASCADE;
DROP TABLE IF EXISTS item_reference_type CASCADE;
DROP TABLE IF EXISTS item_render_metadata CASCADE;
DROP TABLE IF EXISTS measurement_unit CASCADE;
DROP TABLE IF EXISTS null_value_type CASCADE;
DROP TABLE IF EXISTS oc_qrtz_blob_triggers CASCADE;
DROP TABLE IF EXISTS oc_qrtz_calendars CASCADE;
DROP TABLE IF EXISTS oc_qrtz_cron_triggers CASCADE;
DROP TABLE IF EXISTS oc_qrtz_fired_triggers CASCADE;
DROP TABLE IF EXISTS oc_qrtz_job_details CASCADE;
DROP TABLE IF EXISTS oc_qrtz_locks CASCADE;
DROP TABLE IF EXISTS oc_qrtz_paused_trigger_grps CASCADE;
DROP TABLE IF EXISTS oc_qrtz_scheduler_state CASCADE;
DROP TABLE IF EXISTS oc_qrtz_simple_triggers CASCADE;
DROP TABLE IF EXISTS oc_qrtz_simprop_triggers CASCADE;
DROP TABLE IF EXISTS oc_qrtz_triggers CASCADE;
DROP TABLE IF EXISTS openclinica_version CASCADE;
DROP TABLE IF EXISTS "password";
DROP TABLE IF EXISTS resolution_status CASCADE;
DROP TABLE IF EXISTS response_set CASCADE;
DROP TABLE IF EXISTS response_type CASCADE;
DROP TABLE IF EXISTS "rule";
DROP TABLE IF EXISTS rule_action CASCADE;
DROP TABLE IF EXISTS rule_action_property CASCADE;
DROP TABLE IF EXISTS rule_action_run CASCADE;
DROP TABLE IF EXISTS rule_action_run_log CASCADE;
DROP TABLE IF EXISTS rule_expression CASCADE;
DROP TABLE IF EXISTS rule_set CASCADE;
DROP TABLE IF EXISTS rule_set_audit CASCADE;
DROP TABLE IF EXISTS rule_set_rule CASCADE;
DROP TABLE IF EXISTS rule_set_rule_audit CASCADE;
DROP TABLE IF EXISTS scd_item_metadata CASCADE;
DROP TABLE IF EXISTS section CASCADE;
DROP TABLE IF EXISTS status CASCADE;
DROP TABLE IF EXISTS study CASCADE;
DROP TABLE IF EXISTS study_event CASCADE;
DROP TABLE IF EXISTS study_event_definition CASCADE;
DROP TABLE IF EXISTS study_group CASCADE;
DROP TABLE IF EXISTS study_group_class CASCADE;
DROP TABLE IF EXISTS study_module_status CASCADE;
DROP TABLE IF EXISTS study_parameter CASCADE;
DROP TABLE IF EXISTS study_parameter_value CASCADE;
DROP TABLE IF EXISTS study_subject CASCADE;
DROP TABLE IF EXISTS study_subject_id CASCADE;
DROP TABLE IF EXISTS study_type CASCADE;
DROP TABLE IF EXISTS study_user_role CASCADE;
DROP TABLE IF EXISTS subject CASCADE;
DROP TABLE IF EXISTS subject_event_status CASCADE;
DROP TABLE IF EXISTS subject_group_map CASCADE;
DROP TABLE IF EXISTS "system";
DROP TABLE IF EXISTS system_group CASCADE;
DROP TABLE IF EXISTS term CASCADE;
DROP TABLE IF EXISTS term_element CASCADE;
DROP TABLE IF EXISTS usage_statistics_data CASCADE;
DROP TABLE IF EXISTS user_account CASCADE;
DROP TABLE IF EXISTS user_role CASCADE;
DROP TABLE IF EXISTS user_type CASCADE;
DROP TABLE IF EXISTS versioning_map CASCADE;
DROP TABLE IF EXISTS widget CASCADE;
DROP TABLE IF EXISTS widgets_layout CASCADE;



--Drop Sequence

DROP SEQUENCE IF EXISTS archived_dataset_file_archived_dataset_file_id_seq CASCADE;
DROP SEQUENCE IF EXISTS audit_event_audit_id_seq CASCADE;
DROP SEQUENCE IF EXISTS audit_log_event_audit_id_seq CASCADE;
DROP SEQUENCE IF EXISTS audit_log_event_type_audit_log_event_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS audit_log_randomization_id_seq CASCADE;
DROP SEQUENCE IF EXISTS audit_user_login_id_seq CASCADE;
DROP SEQUENCE IF EXISTS authorities_id_seq CASCADE;
DROP SEQUENCE IF EXISTS coded_item_element_id_seq CASCADE;
DROP SEQUENCE IF EXISTS coded_item_id_seq CASCADE;
DROP SEQUENCE IF EXISTS completion_status_completion_status_id_seq CASCADE;
DROP SEQUENCE IF EXISTS configuration_id_seq CASCADE;
DROP SEQUENCE IF EXISTS crf_crf_id_seq CASCADE;
DROP SEQUENCE IF EXISTS crf_oid_id_seq CASCADE;
DROP SEQUENCE IF EXISTS crf_version_crf_version_id_seq CASCADE;
DROP SEQUENCE IF EXISTS crf_version_oid_id_seq CASCADE;
DROP SEQUENCE IF EXISTS crfs_masking_id_seq CASCADE;
DROP SEQUENCE IF EXISTS dataset_dataset_id_seq CASCADE;
DROP SEQUENCE IF EXISTS dataset_item_status_dataset_item_status_id_seq CASCADE;
DROP SEQUENCE IF EXISTS dictionary_id_seq CASCADE;
DROP SEQUENCE IF EXISTS discrepancy_description_id_seq CASCADE;
DROP SEQUENCE IF EXISTS discrepancy_note_discrepancy_note_id_seq CASCADE;
DROP SEQUENCE IF EXISTS discrepancy_note_type_discrepancy_note_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS dyn_item_form_metadata_id_seq CASCADE;
DROP SEQUENCE IF EXISTS dyn_item_group_metadata_id_seq CASCADE;
DROP SEQUENCE IF EXISTS dynamic_event_dynamic_event_id_seq CASCADE;
DROP SEQUENCE IF EXISTS edc_item_metadata_id_seq CASCADE;
DROP SEQUENCE IF EXISTS event_crf_event_crf_id_seq CASCADE;
DROP SEQUENCE IF EXISTS event_crf_section_id_seq CASCADE;
DROP SEQUENCE IF EXISTS event_definition_crf_event_definition_crf_id_seq CASCADE;
DROP SEQUENCE IF EXISTS export_format_export_format_id_seq CASCADE;
DROP SEQUENCE IF EXISTS generic_oid_id_seq CASCADE;
DROP SEQUENCE IF EXISTS group_class_types_group_class_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_data_item_data_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_data_type_item_data_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_form_metadata_item_form_metadata_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_group_item_group_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_group_metadata_item_group_metadata_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_group_oid_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_item_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_oid_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_reference_type_item_reference_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS item_render_metadata_id_seq CASCADE;
DROP SEQUENCE IF EXISTS measurement_unit_id_seq CASCADE;
DROP SEQUENCE IF EXISTS measurement_unit_oid_id_seq CASCADE;
DROP SEQUENCE IF EXISTS null_value_type_null_value_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS openclinica_version_id_seq CASCADE;
DROP SEQUENCE IF EXISTS password_passwd_id_seq CASCADE;
DROP SEQUENCE IF EXISTS resolution_status_resolution_status_id_seq CASCADE;
DROP SEQUENCE IF EXISTS response_set_response_set_id_seq CASCADE;
DROP SEQUENCE IF EXISTS response_type_response_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_action_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_action_property_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_action_run_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_action_run_log_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_expression_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_set_audit_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_set_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_set_rule_audit_id_seq CASCADE;
DROP SEQUENCE IF EXISTS rule_set_rule_id_seq CASCADE;
DROP SEQUENCE IF EXISTS scd_item_metadata_id_seq CASCADE;
DROP SEQUENCE IF EXISTS section_section_id_seq CASCADE;
DROP SEQUENCE IF EXISTS status_status_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_event_definition_oid_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_event_definition_study_event_definition_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_event_study_event_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_group_class_study_group_class_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_group_study_group_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_module_status_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_oid_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_parameter_study_parameter_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_parameter_value_study_parameter_value_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_study_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_subject_id_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_subject_oid_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_subject_study_subject_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_type_study_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS study_user_role_study_user_role_id_seq CASCADE;
DROP SEQUENCE IF EXISTS subject_event_status_subject_event_status_id_seq CASCADE;
DROP SEQUENCE IF EXISTS subject_group_map_subject_group_map_id_seq CASCADE;
DROP SEQUENCE IF EXISTS subject_subject_id_seq CASCADE;
DROP SEQUENCE IF EXISTS system_group_id_seq CASCADE;
DROP SEQUENCE IF EXISTS system_id_seq CASCADE;
DROP SEQUENCE IF EXISTS term_element_id_seq CASCADE;
DROP SEQUENCE IF EXISTS term_id_seq CASCADE;
DROP SEQUENCE IF EXISTS usage_statistics_data_id_seq CASCADE;
DROP SEQUENCE IF EXISTS user_account_user_id_seq CASCADE;
DROP SEQUENCE IF EXISTS user_role_role_id_seq CASCADE;
DROP SEQUENCE IF EXISTS user_type_user_type_id_seq CASCADE;
DROP SEQUENCE IF EXISTS widget_id_seq CASCADE;
DROP SEQUENCE IF EXISTS widgets_layout_id_seq CASCADE;


--Drop Functions
DROP FUNCTION IF EXISTS disable_event_crf(integer, integer, integer, integer);
DROP FUNCTION IF EXISTS disable_event_crfs_by_crf_version(integer, integer, integer, integer);
DROP FUNCTION IF EXISTS disable_event_crfs_by_study_event(integer, integer, integer, integer);
DROP FUNCTION IF EXISTS disable_event_crfs_by_study_event_and_crf_oid(integer, text, integer, integer, integer);
DROP FUNCTION IF EXISTS enable_event_crf(integer, integer, integer);
DROP FUNCTION IF EXISTS enable_event_crfs_by_crf_version(integer, integer, integer);
DROP FUNCTION IF EXISTS enable_event_crfs_by_study_event(integer, integer, integer);
DROP FUNCTION IF EXISTS enable_event_crfs_by_study_event_and_crf_oid(integer, text, integer, integer);

DROP FUNCTION IF EXISTS enable_item_data(integer, integer);
DROP FUNCTION IF EXISTS fix_duplicates_in_study_defs();
DROP FUNCTION IF EXISTS fix_orders();
DROP FUNCTION IF EXISTS fix_rule_referencing_cross_study();
DROP FUNCTION IF EXISTS get_from_states(text, integer, integer, integer);
DROP FUNCTION IF EXISTS revert_from_states(text, integer, timestamp with time zone, integer);
DROP FUNCTION IF EXISTS save_partial_section_info(integer, integer);
DROP FUNCTION IF EXISTS update_states(text, integer, integer);

--Drop Triggers
DROP FUNCTION IF EXISTS event_crf_trigger() CASCADE;
DROP FUNCTION IF EXISTS event_definition_crf_trigger() CASCADE;
DROP FUNCTION IF EXISTS global_subject_trigger() CASCADE;
DROP FUNCTION IF EXISTS item_data_initial_trigger() CASCADE;
DROP FUNCTION IF EXISTS item_data_trigger() CASCADE;
DROP FUNCTION IF EXISTS populate_ssid_in_didm_trigger() CASCADE;
DROP FUNCTION IF EXISTS repeating_item_data_trigger() CASCADE;
DROP FUNCTION IF EXISTS study_event_trigger() CASCADE;
DROP FUNCTION IF EXISTS study_subject_trigger() CASCADE;
DROP FUNCTION IF EXISTS subject_group_assignment_trigger() CASCADE;
DROP FUNCTION IF EXISTS update_event_crf_status() CASCADE;

--Drop VIEWS
DROP VIEW IF EXISTS dn_age_days;
