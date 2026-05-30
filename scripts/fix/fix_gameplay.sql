-- 黄鹤楼
DELETE FROM Buildings WHERE BuildingType = 'BUILDING_YELLOW_CRANE';
DELETE FROM Building_GreatPersonPoints WHERE BuildingType = 'BUILDING_YELLOW_CRANE';
DELETE FROM Building_GreatWorks WHERE BuildingType = 'BUILDING_YELLOW_CRANE';
DELETE FROM Building_ValidTerrains WHERE BuildingType = 'BUILDING_YELLOW_CRANE';
DELETE FROM BuildingModifiers WHERE BuildingType = 'BUILDING_YELLOW_CRANE';

-- 路德维希二世
DELETE FROM Leaders WHERE LeaderType = 'LEADER_LUDWIG';
DELETE FROM AgendaPreferredLeaders WHERE AgendaType = 'AGENDA_WONDER_ADVOCATE';
DELETE FROM RandomAgendas WHERE AgendaType = 'AGENDA_WONDER_ADVOCATE';
DELETE FROM CivilizationLeaders WHERE LeaderType = 'LEADER_LUDWIG';
DELETE FROM FavoredReligions WHERE LeaderType = 'LEADER_LUDWIG';
DELETE FROM HistoricalAgendas WHERE LeaderType = 'LEADER_LUDWIG';
DELETE FROM LeaderQuotes WHERE LeaderType = 'LEADER_LUDWIG';
DELETE FROM LeaderTraits WHERE LeaderType = 'LEADER_LUDWIG';

-- 普雷斯拉夫/加拉太
UPDATE Civilizations
SET 
    CivilizationType = REPLACE(CivilizationType, 'LJUBLJANA', 'BULGARIA_CS'),
    Name = REPLACE(Name, 'LJUBLJANA', 'BULGARIA_CS'),
    Description = REPLACE(Description, 'LJUBLJANA', 'BULGARIA_CS'),
    Adjective = REPLACE(Adjective, 'LJUBLJANA', 'BULGARIA_CS')
WHERE CivilizationType = 'CIVILIZATION_PRESLAV';

