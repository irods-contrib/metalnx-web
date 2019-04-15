--
-- PostgreSQL database dump
--


--
-- Name: group_bookmarks; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE group_bookmarks (
    id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    is_collection boolean,
    is_notified boolean,
    path character varying(512) NOT NULL,
    group_id bigint NOT NULL
);



--
-- Name: group_bookmarks_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE group_bookmarks_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp without time zone,
    is_collection boolean,
    is_notified boolean,
    path character varying(512),
    group_id bigint
);

--
-- Name: group_bookmarks_id_seq; Type: SEQUENCE; Schema: public; Owner: irodsext
--

CREATE SEQUENCE group_bookmarks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: group_bookmarks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: irodsext
--

ALTER SEQUENCE group_bookmarks_id_seq OWNED BY group_bookmarks.id;


--
-- Name: groups; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE groups (
    id bigint NOT NULL,
    additional_info character varying(60),
    data_grid_id bigint NOT NULL,
    groupname character varying(60) NOT NULL
);


--
-- Name: groups_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE groups_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    additional_info character varying(60),
    data_grid_id bigint,
    groupname character varying(60)
);

--
-- Name: groups_id_seq; Type: SEQUENCE; Schema: public; Owner: irodsext
--

CREATE SEQUENCE groups_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: groups_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: irodsext
--

ALTER SEQUENCE groups_id_seq OWNED BY groups.id;


--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: irodsext
--

CREATE SEQUENCE hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

--
-- Name: metadata_fields; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE metadata_fields (
    id bigint NOT NULL,
    attribute character varying(60),
    attribute_unit character varying(60),
    attribute_value character varying(60),
    end_range real,
    field_order integer,
    start_range real,
    template_id bigint NOT NULL
);


--
-- Name: metadata_fields_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE metadata_fields_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    attribute character varying(60),
    attribute_unit character varying(60),
    attribute_value character varying(60),
    end_range real,
    field_order integer,
    start_range real,
    template_id bigint
);


--
-- Name: metadata_fields_id_seq; Type: SEQUENCE; Schema: public; Owner: irodsext
--

CREATE SEQUENCE metadata_fields_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: metadata_fields_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: irodsext
--

ALTER SEQUENCE metadata_fields_id_seq OWNED BY metadata_fields.id;


--
-- Name: revinfo; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE revinfo (
    rev integer NOT NULL,
    revtstmp bigint
);


--
-- Name: template_fields; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE template_fields (
    template_field_id bigint NOT NULL,
    max_attr_length integer NOT NULL,
    max_unt_length integer NOT NULL,
    max_val_length integer NOT NULL,
    attribute character varying(100),
    attribute_unit character varying(100),
    attribute_value character varying(100),
    end_range real,
    field_order integer,
    start_range real,
    template_id bigint NOT NULL
);


--
-- Name: template_fields_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE template_fields_aud (
    template_field_id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    max_attr_length integer,
    max_unt_length integer,
    max_val_length integer,
    attribute character varying(100),
    attribute_unit character varying(100),
    attribute_value character varying(100),
    end_range real,
    field_order integer,
    start_range real,
    template_id bigint
);

--
-- Name: template_fields_template_field_id_seq; Type: SEQUENCE; Schema: public; Owner: irodsext
--

CREATE SEQUENCE template_fields_template_field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: template_fields_template_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: irodsext
--

ALTER SEQUENCE template_fields_template_field_id_seq OWNED BY template_fields.template_field_id;


--
-- Name: templates; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE templates (
    template_id bigint NOT NULL,
    access_type character varying(32),
    create_ts timestamp without time zone NOT NULL,
    description character varying(512) NOT NULL,
    ismodified boolean NOT NULL,
    modify_ts timestamp without time zone NOT NULL,
    owner character varying(100) NOT NULL,
    template_name character varying(100) NOT NULL,
    usage_info character varying(100),
    version integer
);



--
-- Name: templates_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE templates_aud (
    template_id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    access_type character varying(32),
    create_ts timestamp without time zone,
    description character varying(512),
    ismodified boolean,
    modify_ts timestamp without time zone,
    owner character varying(100),
    template_name character varying(100),
    usage_info character varying(100),
    version integer
);


--
-- Name: templates_template_id_seq; Type: SEQUENCE; Schema: public; Owner: irodsext
--

CREATE SEQUENCE templates_template_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: templates_template_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: irodsext
--

ALTER SEQUENCE templates_template_id_seq OWNED BY templates.template_id;


--
-- Name: user_bookmarks; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE user_bookmarks (
    id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    is_collection boolean NOT NULL,
    is_notified boolean,
    name character varying(512) NOT NULL,
    path character varying(512) NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: user_bookmarks_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE user_bookmarks_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp without time zone,
    is_collection boolean,
    is_notified boolean,
    name character varying(512),
    path character varying(512),
    user_id bigint
);


--
-- Name: user_bookmarks_id_seq; Type: SEQUENCE; Schema: public; Owner: irodsext
--

CREATE SEQUENCE user_bookmarks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_bookmarks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: irodsext
--

ALTER SEQUENCE user_bookmarks_id_seq OWNED BY user_bookmarks.id;


--
-- Name: user_favorites; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE user_favorites (
    id bigint NOT NULL,
    created_at timestamp without time zone NOT NULL,
    is_collection boolean,
    name character varying(512) NOT NULL,
    path character varying(512) NOT NULL,
    path_hash integer NOT NULL,
    user_id bigint NOT NULL
);


--
-- Name: user_favorites_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE user_favorites_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp without time zone,
    is_collection boolean,
    name character varying(512),
    path character varying(512),
    path_hash integer,
    user_id bigint
);


--
-- Name: user_favorites_id_seq; Type: SEQUENCE; Schema: public; Owner: irodsext
--

CREATE SEQUENCE user_favorites_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: user_favorites_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: irodsext
--

ALTER SEQUENCE user_favorites_id_seq OWNED BY user_favorites.id;


--
-- Name: user_profile; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE user_profile (
    id bigint NOT NULL,
    description character varying(512) NOT NULL,
    profile_name character varying(64) NOT NULL
);


--
-- Name: user_profile_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE user_profile_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    description character varying(512),
    profile_name character varying(64)
);


--
-- Name: user_profile_groups; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE user_profile_groups (
    profile_id bigint NOT NULL,
    group_id bigint NOT NULL
);


--
-- Name: user_profile_groups_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE user_profile_groups_aud (
    rev integer NOT NULL,
    group_id bigint NOT NULL,
    profile_id bigint NOT NULL,
    revtype smallint
);


--
-- Name: users; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE users (
    id bigint NOT NULL,
    additional_info character varying(128),
    user_company character varying(60),
    data_grid_id bigint NOT NULL,
    user_department character varying(60),
    email character varying(255),
    enabled boolean NOT NULL,
    first_name character varying(255),
    forcefileoverwriting boolean NOT NULL,
    last_name character varying(255),
    locale character varying(255),
    organizational_role character varying(60),
    password character varying(255),
    user_title character varying(60),
    user_type character varying(60) NOT NULL,
    username character varying(60) NOT NULL,
    userprofile_id bigint
);


--
-- Name: users_aud; Type: TABLE; Schema: public; Owner: irodsext
--

CREATE TABLE users_aud (
    id bigint NOT NULL,
    rev integer NOT NULL,
    revtype smallint,
    additional_info character varying(128),
    user_company character varying(60),
    data_grid_id bigint,
    user_department character varying(60),
    email character varying(255),
    enabled boolean,
    first_name character varying(255),
    forcefileoverwriting boolean,
    last_name character varying(255),
    locale character varying(255),
    organizational_role character varying(60),
    password character varying(255),
    user_title character varying(60),
    user_type character varying(60),
    username character varying(60),
    userprofile_id bigint
);


--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: irodsext
--

CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: irodsext
--

ALTER SEQUENCE users_id_seq OWNED BY users.id;


--
-- Name: group_bookmarks id; Type: DEFAULT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY group_bookmarks ALTER COLUMN id SET DEFAULT nextval('group_bookmarks_id_seq'::regclass);


--
-- Name: groups id; Type: DEFAULT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY groups ALTER COLUMN id SET DEFAULT nextval('groups_id_seq'::regclass);


--
-- Name: metadata_fields id; Type: DEFAULT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY metadata_fields ALTER COLUMN id SET DEFAULT nextval('metadata_fields_id_seq'::regclass);


--
-- Name: template_fields template_field_id; Type: DEFAULT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY template_fields ALTER COLUMN template_field_id SET DEFAULT nextval('template_fields_template_field_id_seq'::regclass);


--
-- Name: templates template_id; Type: DEFAULT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY templates ALTER COLUMN template_id SET DEFAULT nextval('templates_template_id_seq'::regclass);


--
-- Name: user_bookmarks id; Type: DEFAULT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_bookmarks ALTER COLUMN id SET DEFAULT nextval('user_bookmarks_id_seq'::regclass);


--
-- Name: user_favorites id; Type: DEFAULT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_favorites ALTER COLUMN id SET DEFAULT nextval('user_favorites_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);


--
-- Name: group_bookmarks_aud group_bookmarks_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY group_bookmarks_aud
    ADD CONSTRAINT group_bookmarks_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: group_bookmarks group_bookmarks_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY group_bookmarks
    ADD CONSTRAINT group_bookmarks_pkey PRIMARY KEY (id);


--
-- Name: groups_aud groups_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY groups_aud
    ADD CONSTRAINT groups_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: groups groups_data_grid_id_key; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT groups_data_grid_id_key UNIQUE (data_grid_id);


--
-- Name: groups groups_groupname_key; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT groups_groupname_key UNIQUE (groupname);


--
-- Name: groups groups_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY groups
    ADD CONSTRAINT groups_pkey PRIMARY KEY (id);


--
-- Name: metadata_fields_aud metadata_fields_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY metadata_fields_aud
    ADD CONSTRAINT metadata_fields_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: metadata_fields metadata_fields_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY metadata_fields
    ADD CONSTRAINT metadata_fields_pkey PRIMARY KEY (id);


--
-- Name: revinfo revinfo_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY revinfo
    ADD CONSTRAINT revinfo_pkey PRIMARY KEY (rev);


--
-- Name: template_fields_aud template_fields_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY template_fields_aud
    ADD CONSTRAINT template_fields_aud_pkey PRIMARY KEY (template_field_id, rev);


--
-- Name: template_fields template_fields_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY template_fields
    ADD CONSTRAINT template_fields_pkey PRIMARY KEY (template_field_id);


--
-- Name: templates_aud templates_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY templates_aud
    ADD CONSTRAINT templates_aud_pkey PRIMARY KEY (template_id, rev);


--
-- Name: templates templates_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY templates
    ADD CONSTRAINT templates_pkey PRIMARY KEY (template_id);


--
-- Name: templates templates_template_name_key; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY templates
    ADD CONSTRAINT templates_template_name_key UNIQUE (template_name);


--
-- Name: user_bookmarks_aud user_bookmarks_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_bookmarks_aud
    ADD CONSTRAINT user_bookmarks_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: user_bookmarks user_bookmarks_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_bookmarks
    ADD CONSTRAINT user_bookmarks_pkey PRIMARY KEY (id);


--
-- Name: user_favorites_aud user_favorites_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_favorites_aud
    ADD CONSTRAINT user_favorites_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: user_favorites user_favorites_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_favorites
    ADD CONSTRAINT user_favorites_pkey PRIMARY KEY (id);


--
-- Name: user_favorites user_favorites_user_id_path_hash_key; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_favorites
    ADD CONSTRAINT user_favorites_user_id_path_hash_key UNIQUE (user_id, path_hash);


--
-- Name: user_profile_aud user_profile_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_profile_aud
    ADD CONSTRAINT user_profile_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: user_profile_groups_aud user_profile_groups_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_profile_groups_aud
    ADD CONSTRAINT user_profile_groups_aud_pkey PRIMARY KEY (rev, profile_id, group_id);


--
-- Name: user_profile_groups user_profile_groups_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_profile_groups
    ADD CONSTRAINT user_profile_groups_pkey PRIMARY KEY (group_id, profile_id);


--
-- Name: user_profile user_profile_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_profile
    ADD CONSTRAINT user_profile_pkey PRIMARY KEY (id);


--
-- Name: users_aud users_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY users_aud
    ADD CONSTRAINT users_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: users users_data_grid_id_key; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_data_grid_id_key UNIQUE (data_grid_id);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: users users_username_additional_info_key; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_additional_info_key UNIQUE (username, additional_info);


--
-- Name: users users_username_key; Type: CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY users
    ADD CONSTRAINT users_username_key UNIQUE (username);


--
-- Name: users_aud fk154c77d9df74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY users_aud
    ADD CONSTRAINT fk154c77d9df74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: user_profile_groups_aud fk2939ceefdf74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_profile_groups_aud
    ADD CONSTRAINT fk2939ceefdf74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: metadata_fields_aud fk3514ff1adf74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY metadata_fields_aud
    ADD CONSTRAINT fk3514ff1adf74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: groups_aud fk4d80fda5df74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY groups_aud
    ADD CONSTRAINT fk4d80fda5df74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: users fk6a68e08c51618a7; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY users
    ADD CONSTRAINT fk6a68e08c51618a7 FOREIGN KEY (userprofile_id) REFERENCES user_profile(id);


--
-- Name: group_bookmarks_aud fk923fe78edf74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY group_bookmarks_aud
    ADD CONSTRAINT fk923fe78edf74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: template_fields fka00c921e533f5fd; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY template_fields
    ADD CONSTRAINT fka00c921e533f5fd FOREIGN KEY (template_id) REFERENCES templates(template_id);


--
-- Name: user_profile_aud fkad9cac86df74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_profile_aud
    ADD CONSTRAINT fkad9cac86df74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: user_profile_groups fkadd6e01e8b6025b7; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_profile_groups
    ADD CONSTRAINT fkadd6e01e8b6025b7 FOREIGN KEY (group_id) REFERENCES groups(id);


--
-- Name: user_profile_groups fkadd6e01eeef2e9c; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_profile_groups
    ADD CONSTRAINT fkadd6e01eeef2e9c FOREIGN KEY (profile_id) REFERENCES user_profile(id);


--
-- Name: user_favorites_aud fkb3535334df74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_favorites_aud
    ADD CONSTRAINT fkb3535334df74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: template_fields_aud fkc54680efdf74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY template_fields_aud
    ADD CONSTRAINT fkc54680efdf74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: user_bookmarks fkd6d4e689872a21dd; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_bookmarks
    ADD CONSTRAINT fkd6d4e689872a21dd FOREIGN KEY (user_id) REFERENCES users(id);


--
-- Name: group_bookmarks fkefc8203d8b6025b7; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY group_bookmarks
    ADD CONSTRAINT fkefc8203d8b6025b7 FOREIGN KEY (group_id) REFERENCES groups(id);


--
-- Name: user_favorites fkf2ff4ee3872a21dd; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_favorites
    ADD CONSTRAINT fkf2ff4ee3872a21dd FOREIGN KEY (user_id) REFERENCES users(id);


--
-- Name: templates_aud fkf654ac8adf74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY templates_aud
    ADD CONSTRAINT fkf654ac8adf74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- Name: metadata_fields fkf68eddc9533f5fd; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY metadata_fields
    ADD CONSTRAINT fkf68eddc9533f5fd FOREIGN KEY (template_id) REFERENCES templates(template_id);


--
-- Name: user_bookmarks_aud fkfda8a7dadf74e053; Type: FK CONSTRAINT; Schema: public; Owner: irodsext
--

ALTER TABLE ONLY user_bookmarks_aud
    ADD CONSTRAINT fkfda8a7dadf74e053 FOREIGN KEY (rev) REFERENCES revinfo(rev);


--
-- PostgreSQL database dump complete
--

