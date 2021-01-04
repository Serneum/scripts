require 'json'

factions = ['CCC', 'USCR', 'EU', 'Keizai Waza', 'GCC', 'EIC', 'House-9', 'House-4', 'ISS', 'KemVar', 'Sefadu', 'Texico']

factions.each do |f|
  $faction = f
  faction_file = f.downcase.gsub(/\s+/, '-')
  input_json_file = "/home/serneum/#{faction_file}-input.json"
  input_model_data = "/home/serneum/#{faction_file.downcase}-models.csv"
  output_json_file = "/home/serneum/#{faction_file.downcase}-output.json"

  $model_data = File.readlines(input_model_data).map { |line| line.split(',') }

  def update_model(m, i)
    m["Nickname"] = "#{$faction} #{$model_data[i][0].strip}"
    if m["CustomImage"]
      m["CustomImage"]["ImageURL"] = $model_data[i][1].strip
      m["CustomImage"]["ImageSecondaryURL"] = $model_data[i][2].strip
    end

    m["ChildObjects"]&.each do |obj|
      update_model(obj, i) if obj["Name"] == "Figurine_Custom"
    end
  end

  model_json = JSON.parse(File.read(input_json_file))
  model_json.each_with_index do |model, i|
    update_model(model, i)

    model["States"]&.each do |id, state|
      state["Nickname"] = "#{$faction} #{$model_data[i][0].strip}"
      update_model(state, i)
    end
  end

  File.write(output_json_file, JSON.pretty_generate(model_json))
end
