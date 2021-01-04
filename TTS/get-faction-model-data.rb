require 'json'

input_json_file = "/home/serneum/TS_Save_2134.json"

save_data = JSON.parse(File.read(input_json_file))

save_data["ObjectStates"].each do |obj|
  if obj["Name"] == "Custom_Model_Bag"
    faction = obj["Nickname"].gsub(/\s+Deployment/, '').strip
    models_csv = File.open("#{faction.downcase.gsub(/\s+/, '-')}-models.csv", "w")
    puts faction
    obj["ContainedObjects"]&.select { |con| con["Name"] == "Bag" && con["Nickname"] == "#{faction} Minis" }&.last["ContainedObjects"]&.each do |m|
      models_csv << "#{m["Nickname"].gsub(faction, '').strip},#{m["CustomImage"]["ImageURL"]},#{m["CustomImage"]["ImageSecondaryURL"]}\n"
    end
    models_csv.close
  end
end
