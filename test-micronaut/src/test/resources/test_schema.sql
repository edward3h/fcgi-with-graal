CREATE TABLE `user` (
  `id` varchar(36) NOT NULL PRIMARY KEY,
  `name` varchar(255) NOT NULL UNIQUE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO `user` (`id`, `name`) VALUES
('84996679-ada3-4ec3-9ea0-118509dd26d1', 'redelephant783'),
('f13925c8-56b6-44c8-88b4-73f2f33cf406', 'heavypanda563'),
('20c3427f-1963-4b89-abf8-10ae757443e5', 'orangepeacock224'),
('35bfe6a0-4a92-46d4-a77b-c2bb99de9e9c', 'greenwolf648'),
('c071df3f-de26-4b49-87d4-a69682ee7ecc', 'bluepanda338');